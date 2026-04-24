@file:Suppress("SameParameterValue")

package container.monolith

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.system.exitProcess


suspend fun main() {
    val monolith = instantiate()

    GlobalScope.launch {
        try {
            monolith.startWorkers()
        } catch (t: Throwable) {
            t.printStackTrace()
            exitProcess(-1)
        }
    }

    monolith.startKtorServer(
        host = MonolithDefaults.host,
        port = MonolithDefaults.port,
        wait = true
    )
}
