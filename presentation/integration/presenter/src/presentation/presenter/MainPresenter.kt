package presentation.presenter


class MainPresenter(
    val auth: AuthPresenter,
    val user: UserPresenter,
    val post: PostPresenter,
) : AuthPresenter by auth,
    UserPresenter by user,
    PostPresenter by post
