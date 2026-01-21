package presentation.mapper

import y9to.api.types.Post
import y9to.api.types.PostContent
import y9to.api.types.PostId
import y9to.api.types.PostReplyHeader
import y9to.api.types.RepostHeader
import backend.core.types.Post as BackendPost
import backend.core.types.PostId as BackendPostId
import backend.core.types.RepostHeader as BackendRepostHeader
import backend.core.types.PostReplyHeader as BackendPostReplyHeader
import backend.core.types.PostContent as BackendPostContent


fun BackendPostId.map() = PostId(long)
fun PostId.map() = BackendPostId(long)


fun BackendPost.map() = Post(
    id = id.map(),
    replyTo = replyTo?.map(),
    author = author.map(),
    publishDate = publishDate,
    lastEditDate = lastEditDate,
    content = content.map(),
)


fun BackendPostContent.map() = when (this) {
    is BackendPostContent.Repost -> map()
    is BackendPostContent.Standalone -> map()
}

fun BackendPostContent.Standalone.map() = PostContent.Standalone(
    text = text,
)

fun BackendPostContent.Repost.map() = PostContent.Repost(
    comment = comment,
    header = header.map(),
)

fun BackendPostReplyHeader.map() = when (this) {
    is BackendPostReplyHeader.Post -> map()
    is BackendPostReplyHeader.DeletedPost -> map()
}

fun BackendPostReplyHeader.Post.map() = PostReplyHeader.Post(
    postId = postId.map(),
    publishDate = publishDate,
    author = author.map(),
)

fun BackendPostReplyHeader.DeletedPost.map() = PostReplyHeader.DeletedPost(
    publishDate = publishDate,
    author = author.map(),
)

fun BackendRepostHeader.map() = when (this) {
    is BackendRepostHeader.Post -> map()
    is BackendRepostHeader.DeletedPost -> map()
}

fun BackendRepostHeader.Post.map() = RepostHeader.Post(
    postId = postId.map(),
    publishDate = publishDate,
    author = author.map(),
    lastEditDate = lastEditDate,
)

fun BackendRepostHeader.DeletedPost.map() = RepostHeader.DeletedPost(
    publishDate = publishDate,
    author = author.map(),
    deletionDate = deletionDate,
    lastEditDate = lastEditDate,
)

