import backend.core.types.SessionId
import container.monolith.instantiate
import presentation.infra.jwtManager.asOk
import y9to.api.types.InputFeed
import y9to.api.types.InputPostContent
import y9to.api.types.InputPostLocation
import y9to.api.types.InputUser
import y9to.api.types.Token
import y9to.api.types.UserId
import y9to.libs.paging.SliceKey
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.io.encoding.Base64


suspend fun main() {
    val monolith = instantiate()
    val token = Token(monolith.jwtManager.issueTokens(SessionId(2)).asOk().accessToken)

    println(monolith)

    println(monolith.rpc.user.getMyProfile(token))

//    println(monolith.rpc.post.create(
//        token,
//        location = InputPostLocation.Profile(InputUser.Id(UserId(1))),
//        replyTo = null,
//        content = InputPostContent.Standalone("My first profile post")
//    ))

    val slice = monolith.rpc.post.sliceFeed(token, SliceKey.Initialize(InputFeed.Profile(UserId(1))), 1)
    println(slice)
}
