package presentation.assembler

import backend.core.types.InputPostContent
import backend.core.types.PostReference
import backend.core.types.UserReference
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
    override suspend fun resolve(input: InputPost): PostReference? {
        if (input is InputPost.Id)
            return PostReference.Id(input.id.map())

        val authState = authStateOrPut {
            service.auth.getAuthState(sessionId)
                ?: error("Unauthenticated")
        }
        val selfId = authState.userIdOrNull()
            ?: error("Unauthenticated")
        val selfRef = UserReference.Id(selfId)

        return when (input) {
            is InputPost.MyFirstPost -> PostReference.FirstOfAuthor(selfRef)
            is InputPost.MyRandomPost -> PostReference.RandomOfAuthor(selfRef)
            is InputPost.MyLastPost -> PostReference.LastOfAuthor(selfRef)
        }
    }

    context(context: Context)
    override suspend fun InputPostContent(input: y9to.api.types.InputPostContent): InputPostContent? {
        when (input) {
            is y9to.api.types.InputPostContent.Standalone -> {
                return InputPostContent.Standalone(input.text)
            }

            is y9to.api.types.InputPostContent.Repost -> {
                val originalRef = resolve(input.original)
                    ?: return null

                return InputPostContent.Repost(
                    comment = input.comment,
                    original = originalRef,
                )
            }
        }
    }
}
