package y9to.api.controller


class MainController(
    val auth: AuthController,
    val user: UserController,
    val post: PostController,
    val file: FileController,
)
