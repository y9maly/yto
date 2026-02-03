package presentation.presenter


class MainPresenter(
    val auth: AuthPresenter,
    val user: UserPresenter,
    val post: PostPresenter,
    val file: FilePresenter,
)
