package y9to.sdk.internals

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import y9to.api.types.ApiUpdateSubscription
import y9to.api.types.Update
import y9to.libs.stdlib.coroutines.flow.collectIn
import y9to.sdk.Client
import kotlin.time.Duration.Companion.milliseconds


internal class UpdateCenter(
    override val client: Client,
) : ClientOwner {
    val logger = client.logger("UpdateCenter")

    private val _updates = MutableSharedFlow<Update>(extraBufferCapacity = Int.MAX_VALUE)
    val updates = _updates.asSharedFlow()

    /**
     * Eagerly starts listening to the [UpdateCenter.updates] in the provided [scope] and buffers updates.
     * Returned Flow can only be collected once.
     *
     * ```Kotlin
     * val savedFlow = MutableSharedFlow(extraBufferCapacity = Int.MAV_VALUE)
     * [scope].launch {
     *     [UpdateCenter.updates].collect { savedFlow.emit(it) }
     * }
     * return savedFlow
     * ```
     */
    fun saveIn(scope: CoroutineScope) =
        updates.produceIn(scope).receiveAsFlow()

    init {
        client.scope.launch {
            while (true) {
                try {
                    request("UpdateCenter#receive", true) {
                        rpc.update.receive(token).collect {
                            logger.trace { "Update received: $it" }
                            preUpdate(it)
                            check(_updates.tryEmit(it))
                        }
                    }
                } catch (e: Exception) {
                    logger.error(e) { "Failed to receive update" }
                    delay(1000.milliseconds)
                    continue
                }
            }
        }
    }

    private val subscriptionsCounterMutex = Mutex()
    private val subscriptionsCounter = mutableMapOf<ApiUpdateSubscription, Int>()
    private val apiSubscriptionsMutex = Mutex()
    private val apiSubscriptions = mutableSetOf<ApiUpdateSubscription>()
    private val subscriptionsApiCommands = Channel<SubscriptionsApiCommand>(Channel.UNLIMITED)

    private sealed interface SubscriptionsApiCommand {
        data class Subscribe(val subscription: ApiUpdateSubscription) : SubscriptionsApiCommand
        data class Unsubscribe(val subscription: ApiUpdateSubscription) : SubscriptionsApiCommand
    }

    init {
        subscriptionsApiCommands.receiveAsFlow().collectIn(client.scope) { command ->
            while (true) {
                try {
                    when (command) {
                        is SubscriptionsApiCommand.Subscribe -> {
                            val stillDemand = subscriptionsCounterMutex.withLock {
                                command.subscription in subscriptionsCounter
                            }

                            if (!stillDemand)
                                break

                            val hasApiSubscription = apiSubscriptionsMutex.withLock {
                                command.subscription in apiSubscriptions
                            }

                            if (hasApiSubscription)
                                break

                            request("UpdateCenter#subscribe(${command.subscription})") { rpc.update.subscribe(token, command.subscription) }
                            apiSubscriptionsMutex.withLock {
                                apiSubscriptions.add(command.subscription)
                            }
                        }

                        is SubscriptionsApiCommand.Unsubscribe -> {
                            val stillNotDemand = subscriptionsCounterMutex.withLock {
                                command.subscription !in subscriptionsCounter
                            }

                            if (!stillNotDemand)
                                break

                            val hasApiSubscription = apiSubscriptionsMutex.withLock {
                                command.subscription in apiSubscriptions
                            }

                            if (!hasApiSubscription)
                                break

                            request("UpdateCenter#unsubscribe(${command.subscription})") { rpc.update.unsubscribe(token, command.subscription) }
                            apiSubscriptionsMutex.withLock {
                                apiSubscriptions.remove(command.subscription)
                            }
                        }
                    }

                    break
                } catch (t: Throwable) {
                    logger.error(t) { "Failed to execute update command $command: ${t.message}" }
                    if (t is CancellationException) throw t
                    delay(1000.milliseconds)
                }
            }
        }
    }

    private fun Update.invalidatesAccessToken(): Boolean {
        return this is Update.AuthStateChanged
    }

    private fun preUpdate(update: Update) {
        if (update.invalidatesAccessToken())
            client.requestController.invalidateAccessToken()
    }

    suspend fun subscribe(subscription: ApiUpdateSubscription) {
        val needApiSubscribe = subscriptionsCounterMutex.withLock {
            val old = subscriptionsCounter.getOrElse(subscription) { 0 }
            val new = old + 1
            subscriptionsCounter[subscription] = new

            new == 1
        }

        if (needApiSubscribe)
            check(subscriptionsApiCommands.trySend(SubscriptionsApiCommand.Subscribe(subscription)).isSuccess)
    }

    suspend fun unsubscribe(subscription: ApiUpdateSubscription) {
        val needApiUnsubscribe = subscriptionsCounterMutex.withLock {
            val old = subscriptionsCounter.getOrElse(subscription) { 0 }
            val new = old - 1

            if (new < 0)
                error("No active subscription for $subscription")
            else if (new == 0)
                subscriptionsCounter.remove(subscription)
            else
                subscriptionsCounter[subscription] = new

            new == 0
        }

        if (needApiUnsubscribe)
            check(subscriptionsApiCommands.trySend(SubscriptionsApiCommand.Unsubscribe(subscription)).isSuccess)
    }
}
