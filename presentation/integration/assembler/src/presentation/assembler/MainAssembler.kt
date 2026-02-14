package presentation.assembler


class MainAssembler(
    val user: UserAssembler,
    val post: PostAssembler,
    val file: FileAssembler,
) : UserAssembler by user,
    PostAssembler by post,
    FileAssembler by file
