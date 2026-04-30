package y9to.sdk.types


data class PostPermissions(
    val canReply: Boolean,
    val canRepost: Boolean,
    val canEdit: Boolean,
    val canDelete: Boolean,
)
