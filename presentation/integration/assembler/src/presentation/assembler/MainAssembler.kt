package presentation.assembler


class MainAssembler(
    val user: UserAssembler,
    val post: PostAssembler,
) : UserAssembler by user,
    PostAssembler by post
