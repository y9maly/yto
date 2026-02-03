@file:OptIn(DelicateIoApi::class)

package y9to.libs.io.segment

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import y9to.libs.io.internals.DelicateIoApi
import y9to.libs.io.internals.InternalIoApi
import y9to.libs.io.internals.LifecycleIoApi
import y9to.libs.io.internals.checkBounds
import y9to.libs.stdlib.coroutines.NoOpDisposableHandle
import kotlin.jvm.JvmField


class Segment internal constructor(
    @property:DelicateIoApi("Ensure that you dont mutate this ByteArray if immutable = true")
    val byteArray: ByteArray,
    @JvmField @set:InternalIoApi
    var immutable: Boolean,
    @JvmField
    val start: Int,
    @JvmField
    val size: Int,
) {
    private var cowHead: CopyOnWrite? = null
    private var cowTail: CopyOnWrite? = null

    init {
        byteArray.checkBounds(start, size)
    }

    // called only from CopyOnWrite.dispose()
    @DelicateIoApi
    internal fun disposeOnWrite(previous: CopyOnWrite?, next: CopyOnWrite?) {
        if (previous != null) {
            previous.next = next
            if (next != null) {
                next.previous = previous
            } else {
                cowTail = previous
            }
        } else {
            cowHead = next
            if (next != null) {
                next.previous = null
            } else {
                cowTail = null
            }
        }
    }

    /**
     * Must be called only from lifecycle owner while consuming
     */
    @LifecycleIoApi
    fun invokeOnWrite(callback: suspend () -> Unit): DisposableHandle {
        if (immutable) return NoOpDisposableHandle
        val tail = cowTail
        val cow = CopyOnWrite(this, callback, tail, null)
        cowTail = cow
        if (tail == null) {
            cowHead = cow
        } else {
            tail.next = cow
        }
        return cow
    }

    /**
     * Must be called only from lifecycle owner while producing
     */
    @LifecycleIoApi
    suspend fun onWrite() {
        require(mutable) { "Segment must be mutable to be able to write into!" }
        val head = cowHead ?: return
        cowHead = null
        cowTail = null
        supervisorScope {
            var cow: CopyOnWrite? = head
            while (cow != null) {
                val current = cow
                current.disposed = true
                launch(start = CoroutineStart.UNDISPATCHED) {
                    current.callback()
                }
                cow = current.next
            }
        }
    }
}
