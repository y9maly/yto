package domain.service


class MainService(
    val auth: AuthService,
    val user: UserService,
    val post: PostService,
    val file: FileService,
)
