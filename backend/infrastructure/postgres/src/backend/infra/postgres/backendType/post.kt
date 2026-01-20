package backend.infra.postgres.backendType

import backend.core.types.PostContentType


val PostContentType.dbName get() = when (this) {
    PostContentType.Standalone -> "standalone"
    PostContentType.Repost -> "repost"
}
