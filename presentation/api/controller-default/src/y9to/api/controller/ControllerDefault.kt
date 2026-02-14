package y9to.api.controller

import presentation.assembler.MainAssembler
import presentation.presenter.MainPresenter


internal interface ControllerDefault {
    val assembler: MainAssembler
    val presenter: MainPresenter
}

internal inline fun <R> ControllerDefault.context(
    block: context(MainAssembler, MainPresenter) () -> R
): R = block(assembler, presenter)
