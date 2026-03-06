package presentation.assembler

import backend.core.types.InputPostContent
import backend.core.types.PostLink
import backend.core.types.UserLink
import domain.service.MainService
import presentation.integration.context.Context
import presentation.integration.context.elements.authStateOrPut
import presentation.integration.context.elements.sessionId
import presentation.mapper.map
import y9to.api.types.InputPost


class PostAssemblerImpl(
    private val service: MainService,
) : PostAssembler {
    context(context: Context)
    override suspend fun resolve(input: InputPost): PostLink? {
        if (input is InputPost.Id)
            return PostLink.Id(input.id.map())

        val authState = authStateOrPut {
            service.auth.getAuthState(sessionId)
                ?: error("Unauthenticated")
        }
        val selfId = authState.userIdOrNull()
            ?: error("Unauthenticated")
        val selfLink = UserLink.Id(selfId)

        return when (input) {
            is InputPost.MyFirstPost -> PostLink.FirstOfAuthor(selfLink)
            is InputPost.MyRandomPost -> PostLink.RandomOfAuthor(selfLink)
            is InputPost.MyLastPost -> PostLink.LastOfAuthor(selfLink)
        }
    }

    context(context: Context)
    override suspend fun InputPostContent(input: y9to.api.types.InputPostContent): InputPostContent? {
        when (input) {
            is y9to.api.types.InputPostContent.Standalone -> {
                return InputPostContent.Standalone(input.text)
            }

            is y9to.api.types.InputPostContent.Repost -> {
                val originalLink = resolve(input.original)
                    ?: return null

                return InputPostContent.Repost(
                    comment = input.comment,
                    original = originalLink,
                )
            }
        }
    }
}
