package integration.repository

import backend.core.types.ClientId
import backend.core.types.File
import backend.core.types.FileId
import backend.core.types.FileOwner
import backend.core.types.FileType
import backend.core.types.FileTypes
import backend.core.types.ImageFile
import backend.core.types.ImageFileFormat
import backend.core.types.SessionId
import backend.core.types.UserId
import backend.infra.postgres.table.TFile
import backend.infra.postgres.table.TImage
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.intLiteral
import org.jetbrains.exposed.v1.core.sum
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.selectAll
import kotlin.math.exp
import kotlin.time.Instant


class FileRepository internal constructor(private val main: MainRepository) {
    suspend fun exists(id: FileId): Boolean = main.transaction(ReadOnly) {
        TFile.select(intLiteral(1))
            .where { TFile.id eq id.long }
            .count() > 0
    }

    suspend fun totalUserStorageUsed(client: ClientId): Long? = main.transaction(ReadOnly) {
        val user = when (client) {
            is UserId -> client.long
        }

        val bytes = TFile.select(TFile.size_bytes.sum())
            .where { TFile.owner_user eq user }
            .first()[TFile.size_bytes.sum()]

        if (bytes == 0L || bytes == null) {
            if (!main.user.exists(client)) {
                return@transaction null
            }
            return@transaction 0L
        }

        return@transaction bytes
    }

    suspend fun select(id: FileId): File? = main.transaction(ReadOnly) {
        val row = TFile.selectAll()
            .where { TFile.id eq id.long }
            .firstOrNull()
            ?: return@transaction null

        val owner_session = row[TFile.owner_session]
        val owner_user = row[TFile.owner_user] ?: row[TFile.owner_user_grave]
        val owner = when {
            owner_user != null -> FileOwner.User(
                user = UserId(owner_user),
                session = owner_session?.let(::SessionId),
            )

            else -> FileOwner.Unknown
        }

        File(
            id = id,
            uri = row[TFile.uri],
            name = row[TFile.name],
            uploadDate = row[TFile.upload_date],
            expiresAt = row[TFile.expires_at],
            sizeBytes = row[TFile.size_bytes],
            owner = owner,
        )
    }

    suspend fun selectImageFile(file: FileId): ImageFile? = main.transaction(ReadOnly) {
        val row = TImage.selectAll()
            .where { TImage.id eq file.long }
            .firstOrNull()
            ?: return@transaction null
        ImageFile(
            file = file,
            format = ImageFileFormat(row[TImage.format]),
            width = row[TImage.width],
            height = row[TImage.height],
        )
    }

    suspend fun selectFileTypes(file: FileId): FileTypes? = main.transaction(ReadOnly) {
        val imageAsync = async { selectImageFile(file) }

        val image = imageAsync.await()
        val types: Array<in FileType> = arrayOf(image)

        if (types.all { it == null }) {
            if (!exists(file)) {
                return@transaction null
            }
        }

        FileTypes(
            image = image,
        )
    }

    suspend fun insert(
        uri: String,
        name: String?,
        owner: FileOwner,
        uploadDate: Instant,
        expiresAt: Instant?,
        sizeBytes: Long,
        types: FileTypes,
    ): File = main.transaction {
        val fileId = TFile.insertAndGetId {
            it[this.uri] = uri
            it[this.name] = name
            it[this.upload_date] = uploadDate
            it[this.expires_at] = expiresAt
            it[this.size_bytes] = sizeBytes
            when (owner) {
                is FileOwner.User -> {
                    it[this.owner_session] = owner.session?.long
                    it[this.owner_user] = owner.user.long
                }

                is FileOwner.Unknown -> {
                    it[this.owner_session] = null
                    it[this.owner_user] = null
                }
            }
        }.value

        launch {
            if (types.image == null)
                return@launch
            val image = types.image!!

            TImage.insert {
                it[this.id] = fileId
                it[this.format] = image.format.name
                it[this.width] = image.width
                it[this.height] = image.height
            }
        }

        File(
            id = FileId(fileId),
            uri = uri,
            name = name,
            uploadDate = uploadDate,
            expiresAt = expiresAt,
            sizeBytes = sizeBytes,
            owner = owner,
        )
    }
}
