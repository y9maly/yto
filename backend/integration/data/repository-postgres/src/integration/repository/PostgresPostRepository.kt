@file:Suppress("LocalVariableName", "UnnecessaryVariable")

package integration.repository

import backend.core.types.*
import backend.infra.postgres.table.TPost
import backend.infra.postgres.table.TPostRepost
import backend.infra.postgres.table.TPostStandalone
import backend.infra.postgres.view.VPost
import integration.repository.PostRepository.SliceOptions
import integration.repository.input.InputPostContent
import integration.repository.input.InputPostLocation
import integration.repository.input.type
import integration.repository.internals.PredicateOp
import integration.repository.internals.RandomFunction
import integration.repository.result.DeletePostError
import integration.repository.result.DeletePostResult
import integration.repository.result.CreatePostError
import integration.repository.result.CreatePostResult
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.r2dbc.*
import y9to.libs.paging.*
import y9to.libs.paging.Slice
import y9to.libs.stdlib.asError
import y9to.libs.stdlib.asOk
import kotlin.time.Instant


internal class PostgresPostRepository internal constructor(private val main: MainRepository) : PostRepository {
    @Serializable
    private data class CursorPayload(
        val predicate: PostPredicate, // from SliceOptions
        val date: Instant,
        val cursor: PostId,
    )

    override suspend fun resolve(ref: PostRef): PostId? = main.transaction(ReadOnly) {
        val query = VPost.select(VPost.id)
            .limit(1)

        when (ref) {
            is PostRef.FirstOfAuthor -> {
                val author = main.user.get(ref.author)?.id
                    ?: return@transaction null

                query
                    .where(VPost.author_id eq author.long)
                    .orderBy(VPost.created_at to SortOrder.ASC)
            }

            is PostRef.RandomOfAuthor -> {
                val author = main.user.get(ref.author)?.id
                    ?: return@transaction null

                query
                    .where(VPost.author_id eq author.long)
                    .orderBy(RandomFunction() to SortOrder.ASC)
            }

            is PostRef.LastOfAuthor -> {
                val author = main.user.get(ref.author)?.id
                    ?: return@transaction null

                query
                    .where(VPost.author_id eq author.long)
                    .orderBy(VPost.created_at to SortOrder.DESC)
            }
        }

        val row = query.firstOrNull() ?: return@transaction null

        PostId(row[VPost.id].value)
    }

    override suspend fun get(id: PostId): Post? = main.transaction(ReadOnly) {
        val query = VPost.selectAll()
            .where(VPost.id eq id.long)
            .limit(1)
        val row = query.firstOrNull() ?: return@transaction null
        fromVPost(row)
    }

    override suspend fun exists(id: PostId): Boolean = main.transaction(ReadOnly) {
        TPost.select(intLiteral(1))
            .where { TPost.id eq id.long }
            .limit(1)
            .count() > 0
    }

    override suspend fun create(
        location: InputPostLocation,
        creationDate: Instant,
        author: UserId,
        replyTo: PostId?,
        content: InputPostContent,
    ): CreatePostResult = main.transaction {
        if (!main.user.exists(author))
            return@transaction CreatePostError.InvalidAuthorLink.asError()

        if (replyTo != null && !exists(replyTo))
            return@transaction CreatePostError.InvalidReplyToPostLink.asError()

        if (location is InputPostLocation.Profile) {
            if (!main.user.exists(location.user))
                return@transaction CreatePostError.InvalidInputLocation.asError()
        }

        val (location_global, location_profile) = when (location) {
            is InputPostLocation.Global -> true to null
            is InputPostLocation.Profile -> false to location.user.long
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
                TPostRepost.insert {
                    it[TPostRepost.id] = id.long
                    it[TPostRepost.comment] = content.comment
                    it[TPostRepost.original] = content.original.long
                }
            }
        }

        get(id)!!.asOk()
    }

    override suspend fun delete(post: PostId): DeletePostResult = main.transaction {
        val rows = TPost.deleteWhere { TPost.id eq post.long }
        if (rows == 0)
            return@transaction DeletePostError.InvalidPostId.asError()
        Unit.asOk()
    }

    override suspend fun slice(
        key: SliceKey<SliceOptions, Cursor>,
        limit: Int,
    ): Slice<Cursor?, Post> = main.transaction(ReadOnly) {
        val key = key.decodePayload<CursorPayload, _>(Json)

        val predicate = key.optionsOrNull()?.predicate ?: key.cursor().predicate

        var query = VPost.selectAll()
            .orderBy(VPost.created_at to SortOrder.DESC, VPost.id to SortOrder.ASC)
            .limit(limit)

        query = query.andFilter(predicate)

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
                    predicate = predicate,
                )
            else null
        Slice(list, Cursor.encodePayloadIfNotNull(Json, nextPayload))
    }
}

private fun Query.andFilter(predicate: PostPredicate): Query = andWhere { PostPredicate(predicate) }

private fun PostPredicate(predicate: PostPredicate) = PredicateOp(predicate) { criteria ->
    when (criteria) {
        is PostCriteria.Id -> {
            VPost.id eq criteria.id.long
        }

        is PostCriteria.ContentType -> {
            VPost.content_type eq criteria.type
        }

        is PostCriteria.Location -> PredicateOp(criteria.location) { locationCriteria ->
            when (locationCriteria) {
                PostLocationCriteria.Global -> {
                    VPost.location_global eq true
                }

                is PostLocationCriteria.Profile -> PredicateOp(locationCriteria.user) { profileCriteria ->
                    when (profileCriteria) {
                        is UserCriteria.Id -> {
                            VPost.location_profile eq profileCriteria.id.long
                        }

                        is UserCriteria.FirstName.Matches -> {
                            VPost.location_profile_first_name match profileCriteria.regex.pattern
                        }

                        is UserCriteria.LastName.Null -> {
                            VPost.location_profile_first_name.isNull()
                        }

                        is UserCriteria.LastName.NotNull -> {
                            VPost.author_last_name.isNotNull()
                        }

                        is UserCriteria.LastName.Matches -> {
                            VPost.location_profile_first_name match profileCriteria.regex.pattern
                        }
                    }
                }

                PostLocationCriteria.AnyProfile -> {
                    VPost.location_profile.isNotNull()
                }

                is PostLocationCriteria.Author -> PredicateOp(locationCriteria.user) { userCriteria ->
                    when (userCriteria) {
                        is UserCriteria.Id -> {
                            VPost.author_id eq userCriteria.id.long
                        }

                        is UserCriteria.FirstName.Matches -> {
                            VPost.author_first_name match userCriteria.regex.pattern
                        }

                        is UserCriteria.LastName.Null -> {
                            VPost.author_last_name.isNull()
                        }

                        is UserCriteria.LastName.NotNull -> {
                            VPost.author_last_name.isNotNull()
                        }

                        is UserCriteria.LastName.Matches -> {
                            VPost.author_last_name match userCriteria.regex.pattern
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
