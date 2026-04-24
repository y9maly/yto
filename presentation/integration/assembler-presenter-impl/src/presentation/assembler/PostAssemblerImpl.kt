package presentation.assembler

import backend.core.types.InputPostContent
import backend.core.types.PostId
import backend.core.types.PostRef
import domain.service.ServiceCollection
import presentation.integration.context.Context
import presentation.integration.context.elements.authStateOrPut
import presentation.integration.context.elements.sessionId
import presentation.mapper.map
import y9to.api.types.InputPost


class PostAssemblerImpl(
    private val service: ServiceCollection,
) : PostAssembler {
    context(context: Context)
    override suspend fun resolve(input: InputPost): PostId? {
        if (input is y9to.api.types.PostId)
            return input.map()

        val authState = authStateOrPut {
            service.auth.getAuthState(sessionId)
                ?: error("Unauthenticated")
        }
        val selfId = authState.userIdOrNull()
            ?: error("Unauthenticated")

        val ref = when (input) {
            is InputPost.MyFirstPost -> PostRef.FirstOfAuthor(selfId)
            is InputPost.MyRandomPost -> PostRef.RandomOfAuthor(selfId)
            is InputPost.MyLastPost -> PostRef.LastOfAuthor(selfId)
        }

        return service.post.resolve(ref)
    }

    context(context: Context)
    override suspend fun PostId(id: y9to.api.types.PostId): PostId {
        return id.map()
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
