package play.api

import y9to.api.types.InputPost
import y9to.api.types.InputPostContent
import y9to.api.types.InputUser
import y9to.api.types.SessionId
import y9to.api.types.Token
import y9to.api.types.UserId
import y9to.libs.stdlib.SpliceKey


val token_invalid = Token(Token.Unsafe(SessionId(0), ""))
val token_1 = Token(Token.Unsafe(SessionId(1), "0.0.1")) // rpc.auth.createSession()
val token_2 = Token(token_1.unsafe.copy(session = SessionId(2)))
val token_3 = Token(token_1.unsafe.copy(session = SessionId(3)))
val token_4 = Token(token_1.unsafe.copy(session = SessionId(4)))
val token_5 = Token(token_1.unsafe.copy(session = SessionId(5)))
val token_6 = Token(token_1.unsafe.copy(session = SessionId(6)))


suspend fun main() {
    val token = token_2

//    println(rpc.auth.getAuthState(token))
//    println(rpc.auth.getSession(token))
//    repeat(2) { println(rpc.auth.needResetLocalCache(token)) }

//    println(rpc.user.get(token, InputUser.Id(UserId(1))))

//    println(rpc.post.get(token, InputPost.MyFirstPost))
//    println(rpc.post.get(token, InputPost.MyRandomPost))
//    println(rpc.post.get(token, InputPost.MyLastPost))
//    println(rpc.post.spliceGlobal(token, limit = 10))
    println(rpc.post.create(token, InputPost.MyLastPost, InputPostContent.Standalone("Реплай на мой предыдущий пост")))
}
