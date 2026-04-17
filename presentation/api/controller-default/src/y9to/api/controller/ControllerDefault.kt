package y9to.api.controller

import presentation.assembler.AssemblerCollection
import presentation.presenter.PresenterCollection


internal interface ControllerDefault {
    val assembler: AssemblerCollection
    val presenter: PresenterCollection
}

internal inline fun <R> ControllerDefault.context(
    block: context(AssemblerCollection, PresenterCollection) () -> R
): R = block(assembler, presenter)
