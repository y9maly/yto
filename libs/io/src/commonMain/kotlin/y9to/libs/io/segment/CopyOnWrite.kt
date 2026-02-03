package y9to.libs.io.segment

import kotlinx.coroutines.DisposableHandle
import y9to.libs.io.internals.DelicateIoApi


internal class CopyOnWrite(
    val segment: Segment,
    // todo idk suspend or not
    val callback: suspend () -> Unit,
    var previous: CopyOnWrite?,
    var next: CopyOnWrite?,
) : DisposableHandle {
    var disposed = false

    @OptIn(DelicateIoApi::class)
    override fun dispose() {
        if (disposed) return
        disposed = true
        segment.disposeOnWrite(previous, next)
    }
}
