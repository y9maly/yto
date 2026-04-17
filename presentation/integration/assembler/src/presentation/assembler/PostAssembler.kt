@file:Suppress("RedundantSuspendModifier")

package presentation.assembler

import backend.core.types.PostId
import presentation.integration.context.Context
import y9to.api.types.InputPost
import y9to.api.types.InputPostContent
import backend.core.types.InputPostContent as DomainInputPostContent


interface PostAssembler {
    context(context: Context)
    suspend fun resolve(input: InputPost): PostId?

    context(context: Context)
    suspend fun InputPostContent(input: InputPostContent): DomainInputPostContent?
}

context(_: Context, assembler: PostAssembler)
suspend fun InputPost.resolve(): PostId? = assembler.resolve(this)

context(_: Context, assembler: PostAssembler)
suspend fun InputPostContent.map(): DomainInputPostContent? = assembler.InputPostContent(this)
