package domain.service


class ServiceCollection(
    val auth: AuthService,
    val user: UserService,
    val post: PostService,
    val file: FileService,
)
