package integration.repository


data class RepositoryCollection(
    val auth: AuthRepository,
    val user: UserRepository,
    val post: PostRepository,
    val file: FileRepository,
)
