@file:Suppress("RedundantSuspendModifier")

package presentation.assembler

import backend.core.reference.PostReference
import presentation.assembler.postAssembler
import presentation.integration.callContext.CallContext
import presentation.integration.callContext.CallContext.Keys
import y9to.api.types.InputPost
import y9to.api.types.InputPostContent
import y9to.libs.stdlib.delegates.static
import backend.core.input.InputPostContent as DomainInputPostContent


interface PostAssembler {
    context(callContext: CallContext)
    suspend fun resolve(input: InputPost): PostReference?

    context(callContext: CallContext)
    suspend fun InputPostContent(input: InputPostContent): DomainInputPostContent?
}

val Keys.postAssembler by static { CallContext.Key<PostAssembler>() }

var CallContext.postAssembler
    get() = contextMap[Keys.postAssembler]
    set(value) { contextMap[Keys.postAssembler] = value }
