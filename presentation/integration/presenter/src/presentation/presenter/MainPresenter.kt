package presentation.presenter


class MainPresenter(
    val auth: AuthPresenter,
    val user: UserPresenter,
    val post: PostPresenter,
    val file: FilePresenter,
) : AuthPresenter by auth,
    UserPresenter by user,
    PostPresenter by post,
    FilePresenter by file
