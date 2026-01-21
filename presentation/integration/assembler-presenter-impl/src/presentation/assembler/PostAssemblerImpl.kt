package presentation.assembler

import backend.core.input.InputPostContent
import backend.core.reference.PostReference
import backend.core.reference.UserReference
import domain.service.MainService
import presentation.integration.callContext.CallContext
import presentation.integration.callContext.elements.authStateOrPut
import presentation.integration.callContext.elements.sessionId
import presentation.mapper.map
import y9to.api.types.InputPost


class PostAssemblerImpl(
    private val service: MainService,
) : PostAssembler {
    context(callContext: CallContext)
    override suspend fun resolve(input: InputPost): PostReference? {
        if (input is InputPost.Id)
            return PostReference.Id(input.id.map())

        val authState = callContext.authStateOrPut {
            service.auth.getAuthState(callContext.sessionId)
                ?: error("Unauthenticated")
        }
        val selfId = authState.userIdOrNull()
            ?: error("Unauthenticated")
        val selfRef = UserReference.Id(selfId)

        return when (input) {
            is InputPost.MyFirstPost -> PostReference.FirstAuthor(selfRef)
            is InputPost.MyRandomPost -> PostReference.RandomAuthor(selfRef)
            is InputPost.MyLastPost -> PostReference.LastAuthor(selfRef)
        }
    }

    context(callContext: CallContext)
    override suspend fun InputPostContent(input: y9to.api.types.InputPostContent): InputPostContent? {
        when (input) {
            is y9to.api.types.InputPostContent.Standalone -> {
                return InputPostContent.Standalone(input.text)
            }

            is y9to.api.types.InputPostContent.Repost -> {
                val originalRef = callContext.postAssembler.resolve(input.original)
                    ?: return null

                return InputPostContent.Repost(
                    comment = input.comment,
                    original = originalRef,
                )
            }
        }
    }
}
