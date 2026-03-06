@file:Suppress("LocalVariableName", "UnnecessaryVariable")

package integration.repository

import backend.core.types.*
import backend.infra.postgres.table.TPost
import backend.infra.postgres.table.TPostRepost
import backend.infra.postgres.table.TPostStandalone
import backend.infra.postgres.view.VGravePost
import backend.infra.postgres.view.VPost
import integration.repository.input.InputPostContent
import integration.repository.input.InputPostLocation
import integration.repository.input.type
import integration.repository.internalResolve.resolve
import integration.repository.internals.FilterOp
import integration.repository.internals.RandomFunction
import integration.repository.internals.andFilter
import integration.repository.result.DeletePostResult
import integration.repository.result.InsertPostError
import integration.repository.result.InsertPostResult
import integration.repository.types.DeletedPost
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.Serializable
import kotlinx.serialization.StringFormat
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.r2dbc.*
import y9to.libs.paging.*
import y9to.libs.paging.Slice
import y9to.libs.stdlib.asError
import y9to.libs.stdlib.asOk
import kotlin.time.Instant


class PostRepository internal constructor(
    private val main: MainRepository,
    private val cursorPayloadFormat: StringFormat = Json,
) {
    @Serializable
    data class SliceOptions(val filter: PostFilter)

    @Serializable
    private data class CursorPayload(
        val filter: PostFilter, // from SliceOptions
        val date: Instant,
        val cursor: PostId,
    )

    suspend fun get(id: PostId) = get(PostLink.Id(id))

    //

    suspend fun get(link: PostLink): Post? = main.transaction(ReadOnly) {
        var query = VPost.selectAll()
            .limit(1)

        query = when (link) {
            is PostLink.Id -> query
                .where(VPost.id eq link.id.long)

            is PostLink.RandomOf -> {
                TODO()
            }

            is PostLink.FirstOfAuthor -> {
                val author = main.user.get(link.author)?.id
                    ?: return@transaction null

                query
                    .where(VPost.author_id eq author.long)
                    .orderBy(VPost.created_at to SortOrder.ASC)
            }

            is PostLink.RandomOfAuthor -> {
                val author = main.user.get(link.author)?.id
                    ?: return@transaction null

                query
                    .where(VPost.author_id eq author.long)
                    .orderBy(RandomFunction() to SortOrder.ASC)
            }

            is PostLink.LastOfAuthor -> {
                val author = main.user.get(link.author)?.id
                    ?: return@transaction null

                query
                    .where(VPost.author_id eq author.long)
                    .orderBy(VPost.created_at to SortOrder.DESC)
            }

            is PostLink.First -> query
                .orderBy(VPost.created_at to SortOrder.ASC)

            is PostLink.Random -> query
                .orderBy(RandomFunction() to SortOrder.ASC)

            is PostLink.Last -> query
                .orderBy(VPost.created_at to SortOrder.DESC)
        }

        val row = query.firstOrNull() ?: return@transaction null
        fromVPost(row)
    }

    suspend fun exists(id: PostId): Boolean = main.transaction(ReadOnly) {
        TPost.select(intLiteral(1))
            .where { TPost.id eq id.long }
            .limit(1)
            .count() > 0
    }

    suspend fun insert(
        location: InputPostLocation,
        creationDate: Instant,
        author: UserLink,
        replyTo: PostLink?,
        content: InputPostContent,
    ): InsertPostResult = main.transaction {
        val replyTo = replyTo?.let {
            main.resolve(replyTo)
                ?: return@transaction InsertPostError.InvalidReplyToPostLink.asError()
        }

        val author = author.let {
            main.resolve(author)
                ?: return@transaction InsertPostError.InvalidAuthorLink.asError()
        }

        //

        if (!main.user.exists(author))
            return@transaction InsertPostError.InvalidAuthorLink.asError()

        if (replyTo != null && !exists(replyTo))
            return@transaction InsertPostError.InvalidReplyToPostLink.asError()

        if (location is InputPostLocation.Profile) {
            if (!main.user.exists(location.user))
                return@transaction InsertPostError.InvalidInputLocation.asError()
        }

        val (location_global, location_profile) = when (location) {
            is InputPostLocation.Global -> true to null

            is InputPostLocation.Profile -> {
                val userId = main.resolve(location.user)
                    ?: return@transaction InsertPostError.InvalidInputLocation.asError()
                false to userId.long
            }
        }

        val id = TPost.insertAndGetId {
            it[TPost.location_global] = location_global
            it[TPost.location_profile] = location_profile
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

            is InputPostContent.Repost -> {
                val originalId = main.resolve(content.original)
                    ?: return@transaction InsertPostError.InvalidInputContent.asError()

                TPostRepost.insert {
                    it[TPostRepost.id] = id.long
                    it[TPostRepost.comment] = content.comment
                    it[TPostRepost.original] = originalId.long
                }
            }
        }

        get(id)!!.asOk()
    }

    suspend fun delete(post: PostLink): DeletePostResult = main.transaction {
        TODO()
    }

    suspend fun slice(
        key: SliceKey<SliceOptions, Cursor>,
        limit: Int,
    ): Slice<Cursor?, Post> = main.transaction(ReadOnly) {
        val key = key.decodePayload<CursorPayload, _>(cursorPayloadFormat)

        val filter = key.optionsOrNull()?.filter ?: key.cursor().filter

        var query = VPost.selectAll()
            .orderBy(VPost.created_at to SortOrder.DESC, VPost.id to SortOrder.ASC)
            .limit(limit)

        query = query.andFilter(filter)

        key.onNext { cursor ->
            query = query.andWhere {
                (VPost.created_at less cursor.date) or
                ((VPost.created_at eq cursor.date) and (VPost.id greater cursor.cursor.long))
            }
        }

        val rows = query.toList()

        val list = coroutineScope {
            rows.map { async { fromVPost(it) } }
                .awaitAll()
        }

        val last = rows.lastOrNull()
        val nextPayload =
            if (last != null && list.size == limit)
                CursorPayload(
                    date = last[VPost.created_at],
                    cursor = PostId(last[VPost.id].value),
                    filter = filter,
                )
            else null
        Slice(list, Cursor.encodePayloadIfNotNull(cursorPayloadFormat, nextPayload))
    }
}

private fun Query.andFilter(filter: PostFilter): Query = andFilter(filter) { predicate ->
    when (predicate) {
        is PostPredicate.Id -> {
            VPost.id eq predicate.id.long
        }

        is PostPredicate.Content -> {
            VPost.content_type eq predicate.type
        }

        is PostPredicate.Location -> FilterOp(predicate.location) { predicate ->
            when (predicate) {
                is PostLocationPredicate.Global -> {
                    VPost.location_global eq true
                }

                is PostLocationPredicate.Profile -> FilterOp(predicate.user) { predicate ->
                    when (predicate) {
                        is UserPredicate.Id -> {
                            VPost.location_profile eq predicate.id.long
                        }

                        is UserPredicate.Ids -> {
                            VPost.location_profile inList predicate.ids.map { it.long }
                        }
                    }
                }
            }
        }
    }
}

private suspend fun PostRepository.fromVPost(row: ResultRow): Post {
    val id = PostId(row[VPost.id].value)
    val publishDate = row[VPost.created_at]
    val lastEditDate = row[VPost.last_edit_date]
    val revision = row[VPost.revision]

    val location = run {
        val location_global = row[VPost.location_global]
        val location_profile = row[VPost.location_profile]

        @Suppress("KotlinConstantConditions")
        when {
            location_global -> PostLocation.Global
            location_profile != null -> PostLocation.Profile(user = UserId(location_profile))
            else -> error(
                "Invalid (location_global, location_profile) values ($location_global, $location_profile). post id=$id."
            )
        }
    }

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
            val original_id = row[VPost.repost_original_id] ?: error("Type is 'repost' but 'original_id' is null")

            val preview = run {
                val created_at = row[VPost.repost_original_created_at] ?: error("Type is 'repost' but 'original_created_at' is null")
                val last_edit_date = row[VPost.repost_original_last_edit_date]
                val author_deleted_at = row[VPost.repost_original_author_deleted_at]
                val author_first_name = row[VPost.repost_original_author_first_name] ?: error("Type is 'repost' but 'original_author_first_name' is null")
                val author_last_name = row[VPost.repost_original_author_last_name]
                val content = this@fromVPost.get(PostId(original_id))?.content

                val isDeleted = content == null
                val deletionDate = original_deleted_at
                val publishDate = created_at
                val lastEditDate = last_edit_date
                val postId = PostId(original_id)
                val author = if (author_deleted_at == null) {
                    // Возможно временно. User id остается в БД навсегда даже после удаления пользователя. Поэтому null здесь никогда не будет.
                    val author_id = row[VPost.repost_original_author_id] ?: error("Type is 'repost' but 'original_author_id' is null")

                    PostAuthorPreview.User(
                        id = UserId(author_id),
                        firstName = author_first_name,
                        lastName = author_last_name,
                    )
                } else {
                    PostAuthorPreview.DeletedUser(
                        firstName = author_first_name,
                        lastName = author_last_name,
                    )
                }

                if (!isDeleted)
                    RepostPreview.Post(
                        postId = postId,
                        author = author,
                        publishDate = publishDate,
                        lastEditDate = lastEditDate,
                        content = content,
                    )
                else
                    RepostPreview.DeletedPost(
                        deletionDate = deletionDate ?: Instant.fromEpochSeconds(0),
                        author = author,
                        publishDate = publishDate,
                        lastEditDate = lastEditDate,
                    )
            }

            PostContent.Repost(
                preview = preview,
                comment = comment,
            )
        }
    }

    return Post(
        id = id,
        revision = revision,
        location = location,
        replyTo = replyHeader,
        author = author,
        publishDate = publishDate,
        lastEditDate = lastEditDate,
        content = content,
    )
}

private fun fromVGravePost(row: ResultRow): DeletedPost {
    val id = PostId(row[VGravePost.id].value)
    val publishDate = row[VGravePost.created_at]
    val lastEditDate = row[VGravePost.last_edit_date]
    val deletionDate = row[VGravePost.deleted_at]

    val author = UserPreview(
        id = UserId(row[VGravePost.author_id]),
        firstName = row[VGravePost.author_first_name],
        lastName = row[VGravePost.author_last_name],
    )

    return DeletedPost(
        id = id,
        author = author,
        publishDate = publishDate,
        lastEditDate = lastEditDate,
        deletionDate = deletionDate,
    )
}
