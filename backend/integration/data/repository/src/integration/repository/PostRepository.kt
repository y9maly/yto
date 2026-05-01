@file:Suppress("LocalVariableName", "UnnecessaryVariable")

package integration.repository

import backend.core.types.*
import integration.repository.input.InputPostContent
import integration.repository.input.InputPostLocation
import integration.repository.result.CreatePostResult
import integration.repository.result.DeletePostResult
import integration.repository.result.EditPostError
import integration.repository.result.EditPostResult
import kotlinx.serialization.Serializable
import y9to.libs.paging.Cursor
import y9to.libs.paging.Slice
import y9to.libs.paging.SliceKey
import y9to.libs.stdlib.optional.Optional
import y9to.libs.stdlib.optional.none
import kotlin.time.Instant


interface PostRepository {
    @Serializable
    data class SliceOptions(val predicate: PostPredicate)

    suspend fun resolve(ref: PostRef): PostId?

    suspend fun get(id: PostId): Post?

    suspend fun exists(id: PostId): Boolean

    suspend fun create(
        location: InputPostLocation,
        creationDate: Instant,
        author: UserId,
        replyTo: PostId?,
        content: InputPostContent,
    ): CreatePostResult

    suspend fun edit(
        post: PostId,
        author: Optional<UserId> = none(),
        replyTo: Optional<PostId?> = none(),
        content: Optional<InputPostContent> = none(),
    ): EditPostResult

    suspend fun delete(post: PostId): DeletePostResult

    suspend fun slice(
        key: SliceKey<SliceOptions, Cursor>,
        limit: Int,
    ): Slice<Cursor?, Post>
}
