package y9to.libs.stdlib.coroutines

import kotlinx.coroutines.DisposableHandle


object NoOpDisposableHandle : DisposableHandle {
    override fun dispose() {}
}
