package y9to.api.controller


class ControllerCollection(
    val auth: AuthController,
    val user: UserController,
    val post: PostController,
    val file: FileController,
    val update: UpdateController,
)
