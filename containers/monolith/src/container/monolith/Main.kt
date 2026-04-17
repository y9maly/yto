@file:Suppress("SameParameterValue")

package container.monolith


suspend fun main() {
    val monolith = instantiate()

    monolith.startKtorServer(
        host = MonolithDefaults.host,
        port = MonolithDefaults.port,
        wait = true
    )
}
