@file:Suppress("LocalVariableName", "UnnecessaryVariable")

package integration.repository

import integration.repository.input.InputPostContent
import integration.repository.input.type
import integration.repository.result.InsertPostError
import integration.repository.result.InsertPostResult
import integration.repository.result.SelectAuthorPostError
import integration.repository.result.SelectAuthorPostResult
import backend.infra.postgres.table.TPost
import backend.infra.postgres.table.TPostRepost
import backend.infra.postgres.table.TPostStandalone
import backend.infra.postgres.view.VPost
import backend.core.types.Post
import backend.core.types.PostContent
import backend.core.types.PostContentType
import backend.core.types.PostId
import backend.core.types.PostReplyHeader
import backend.core.types.RepostHeader
import backend.core.types.UserId
import backend.core.types.UserPreview
import integration.repository.internals.RandomFunction
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.intLiteral
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.r2dbc.andWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.selectAll
import y9to.libs.stdlib.PagingKey
import y9to.libs.stdlib.SerializablePagingKey
import y9to.libs.stdlib.SerializedPagingKey
import y9to.libs.stdlib.Slice
import y9to.libs.stdlib.SpliceKey
import y9to.libs.stdlib.asError
import y9to.libs.stdlib.asOk
import kotlin.io.encoding.Base64
import kotlin.time.Instant


class PostRepository internal constructor(private val main: MainRepository) {
    suspend fun select(id: PostId): Post? = main.transaction(ReadOnly) {
        val row = VPost.selectAll()
            .where { VPost.id eq id.long }
            .singleOrNull()
            ?: return@transaction null
        return@transaction fromViewPost(row)
    }

    suspend fun selectFirstPost(author: UserId): SelectAuthorPostResult = main.transaction(ReadOnly) {
        if (!main.user.exists(author))
            return@transaction SelectAuthorPostError.UnknownAuthorId.asError()
        val row = VPost.selectAll()
            .where { VPost.author_id eq author.long }
            .orderBy(VPost.created_at to SortOrder.ASC)
            .limit(1)
            .firstOrNull()
            ?: return@transaction SelectAuthorPostError.AuthorHasNoPosts.asError()
        val post = fromViewPost(row)
        post.asOk()
    }

    suspend fun selectLastPost(author: UserId): SelectAuthorPostResult = main.transaction(ReadOnly) {
        if (!main.user.exists(author))
            return@transaction SelectAuthorPostError.UnknownAuthorId.asError()
        val row = VPost.selectAll()
            .where { VPost.author_id eq author.long }
            .orderBy(VPost.created_at to SortOrder.DESC)
            .limit(1)
            .firstOrNull()
            ?: return@transaction SelectAuthorPostError.AuthorHasNoPosts.asError()
        val post = fromViewPost(row)
        post.asOk()
    }

    suspend fun selectRandomPost(): Post? = main.transaction(ReadOnly) {
        val row = VPost.selectAll()
            .orderBy(RandomFunction() to SortOrder.ASC)
            .limit(1)
            .firstOrNull()
            ?: return@transaction null
        fromViewPost(row)
    }

    suspend fun selectFirstPost(): Post? = main.transaction(ReadOnly) {
        val row = VPost.selectAll()
            .orderBy(VPost.created_at to SortOrder.ASC)
            .limit(1)
            .firstOrNull()
            ?: return@transaction null
        fromViewPost(row)
    }

    suspend fun selectLastPost(): Post? = main.transaction(ReadOnly) {
        val row = VPost.selectAll()
            .orderBy(VPost.created_at to SortOrder.DESC)
            .limit(1)
            .firstOrNull()
            ?: return@transaction null
        fromViewPost(row)
    }

    suspend fun selectRandomAuthorPost(author: UserId): SelectAuthorPostResult = main.transaction(ReadOnly) {
        if (!main.user.exists(author))
            return@transaction SelectAuthorPostError.UnknownAuthorId.asError()
        val row = VPost.selectAll()
            .where { VPost.author_id eq author.long }
            .orderBy(RandomFunction() to SortOrder.ASC)
            .limit(1)
            .firstOrNull()
            ?: return@transaction SelectAuthorPostError.AuthorHasNoPosts.asError()
        val post = fromViewPost(row)
        post.asOk()
    }

    suspend fun exists(id: PostId): Boolean = main.transaction(ReadOnly) {
        TPost.select(intLiteral(1))
            .where { TPost.id eq id.long }
            .limit(1)
            .count() > 0
    }

    suspend fun insert(
        creationDate: Instant,
        author: UserId,
        replyTo: PostId?,
        content: InputPostContent,
    ): InsertPostResult = main.transaction {
        if (!main.user.exists(author))
            return@transaction InsertPostError.UnknownAuthorId.asError()

        if (replyTo != null && !exists(replyTo))
            return@transaction InsertPostError.UnknownReplyToPostId.asError()

        val id = TPost.insertAndGetId {
            it[TPost.created_at] = creationDate
            it[TPost.author] = author.long
            it[TPost.reply_to] = replyTo?.long
            it[TPost.content_type] = content.type
        }.value.let(::PostId)

        when (content) {
            is InputPostContent.Standalone -> TPostStandalone.insert {
                it[TPostStandalone.id] = id.long
                it[TPostStandalone.text] = content.text
            }

            is InputPostContent.Repost -> TPostRepost.insert {
                it[TPostRepost.id] = id.long
                it[TPostRepost.comment] = content.comment
                it[TPostRepost.original] = content.original.long
            }
        }

        select(id)!!.asOk()
    }

    private data class SpliceGlobalPagingKey(val date: Instant, val cursor: PostId) : SerializablePagingKey {
        override fun serialize() = SerializedPagingKey(
            Base64.UrlSafe.encode("${date.toEpochMilliseconds()}:${cursor.long}".toByteArray())
                .replace('=', '_')
        )

        companion object {
            fun of(pagingKey: PagingKey): SpliceGlobalPagingKey {
                if (pagingKey is SpliceGlobalPagingKey)
                    return pagingKey
                if (pagingKey !is SerializedPagingKey)
                    error("Invalid PagingKey")
                val string = Base64.UrlSafe.decode(pagingKey.string.replace('_', '=')).toString(Charsets.UTF_8)
                return SpliceGlobalPagingKey(
                    Instant.fromEpochMilliseconds(string.substringBefore(":").toLong()),
                    PostId(string.substringAfter(":").toLong())
                )
            }
        }
    }

    suspend fun sliceGlobal(
        key: SpliceKey<Unit>,
        limit: Int,
    ): Slice<Post> = main.transaction(ReadOnly) {
        var query = VPost.selectAll()
            .orderBy(VPost.created_at to SortOrder.DESC, VPost.id to SortOrder.ASC)
            .limit(limit)

        key.fold(
            initialize = {},
            next = {
                val cursor = SpliceGlobalPagingKey.of(it)
                query = query.andWhere {
                    (VPost.created_at less cursor.date) //or
//                    ((VPost.created_at eq cursor.date) and (VPost.id greater cursor.cursor.long))
                }
            }
        )

        val rows = query.toList()

        val list = rows.map(::fromViewPost)
        val last = rows.lastOrNull()
        val nextPagingKey =
            if (last != null && list.size == limit)
                SpliceGlobalPagingKey(last[VPost.created_at], PostId(last[VPost.id].value))
            else null
        Slice(list, nextPagingKey)
    }
}

private fun fromViewPost(row: ResultRow): Post {
    val id = PostId(row[VPost.id].value)
    val publishDate = row[VPost.created_at]
    val lastEditDate = row[VPost.last_edit_date]
    val revision = row[VPost.revision]

    val author = UserPreview(
        id = UserId(row[VPost.author_id]),
        firstName = row[VPost.author_first_name],
        lastName = row[VPost.author_last_name],
    )

    val replyHeader = run {
        val id = row[VPost.reply_id] ?: return@run null
        val deleted_at = row[VPost.reply_deleted_at]
        val created_at = row[VPost.reply_created_at] ?: return@run null
        val author_id = row[VPost.reply_author_id] ?: return@run null
        val author_first_name = row[VPost.reply_author_first_name] ?: return@run null
        val author_last_name = row[VPost.reply_author_last_name]

        val postId = PostId(id)
        val isDeleted = deleted_at != null
        val publishDate = created_at
        val author = UserPreview(
            id = UserId(author_id),
            firstName = author_first_name,
            lastName = author_last_name,
        )

        if (!isDeleted)
            PostReplyHeader.Post(postId, publishDate, author)
        else
            PostReplyHeader.DeletedPost(publishDate, author)
    }

    val content = when (row[VPost.content_type]) {
        PostContentType.Standalone -> {
            val text = row[VPost.standalone_text] ?: error("Type is 'standalone' but 'text' is null")
            PostContent.Standalone(text = text)
        }

        PostContentType.Repost -> {
            val comment = row[VPost.repost_comment]
            val original_deleted_at = row[VPost.repost_original_deleted_at]

            val header = run {
                val original_id = row[VPost.repost_original_id] ?: error("Type is 'repost' but 'original_id' is null")
                val created_at = row[VPost.repost_original_created_at] ?: error("Type is 'repost' but 'original_created_at' is null")
                val last_edit_date = row[VPost.repost_original_last_edit_date]
                // Возможно временно. User id остается в БД навсегда даже после удаления пользователя. Поэтому null здесь никогда не будет.
                val author_id = row[VPost.repost_original_author_id] ?: error("Type is 'repost' but 'original_author_id' is null")
                val author_first_name = row[VPost.repost_original_author_first_name] ?: error("Type is 'repost' but 'original_author_first_name' is null")
                val author_last_name = row[VPost.repost_original_author_last_name]

                val idDeleted = original_deleted_at != null
                val deletionDate = original_deleted_at
                val publishDate = created_at
                val lastEditDate = last_edit_date
                val postId = PostId(original_id)
                val authorId = UserId(author_id)
                val author = UserPreview(
                    id = authorId,
                    firstName = author_first_name,
                    lastName = author_last_name,
                )

                if (!idDeleted)
                    RepostHeader.Post(
                        postId = postId,
                        author = author,
                        publishDate = publishDate,
                        lastEditDate = lastEditDate,
                    )
                else
                    RepostHeader.DeletedPost(
                        deletionDate = deletionDate,
                        author = author,
                        publishDate = publishDate,
                        lastEditDate = lastEditDate,
                    )
            }

            PostContent.Repost(
                header = header,
                comment = comment,
            )
        }
    }

    return Post(
        id = id,
        revision = revision,
        replyTo = replyHeader,
        author = author,
        publishDate = publishDate,
        lastEditDate = lastEditDate,
        content = content,
    )
}
