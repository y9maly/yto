package domain.service

import backend.core.types.*
import domain.service.result.CreatePostResult
import domain.service.result.DeletePostResult
import y9to.libs.paging.Cursor
import y9to.libs.paging.Slice
import y9to.libs.paging.SliceKey


interface PostService {
    suspend fun resolve(ref: PostRef): PostId?
    suspend fun get(id: PostId): Post?

    suspend fun create(
        location: InputPostLocation,
        author: UserId,
        replyTo: PostId?,
        content: InputPostContent,
    ): CreatePostResult

    suspend fun delete(post: PostId): DeletePostResult

    suspend fun sliceGlobal(
        key: SliceKey<Unit, Cursor>,
        limit: Int,
    ): Slice<Cursor?, Post>

    suspend fun sliceProfile(
        key: SliceKey<UserId, Cursor>,
        limit: Int,
    ): Slice<Cursor?, Post>?
}
