package play.sdk

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import y9to.api.types.AuthorizableId
import y9to.api.types.InputPost
import y9to.api.types.UserId
import y9to.libs.stdlib.coroutines.flow.collectIn
import y9to.sdk.createSdkClient


suspend fun main(): Unit = coroutineScope {
    val sdk = createSdkClient("localhost", 8103, "/")
    println(sdk.auth.authState.first())

    sdk.user.me.collectIn(this) {
        println(it)
    }
}
