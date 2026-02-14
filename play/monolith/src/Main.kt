import container.monolith.Token
import container.monolith.instantiate


suspend fun main() {
    val monolith = instantiate()

    println(monolith)

    println(monolith.rpc.user.getMyProfile(Token(2)))
}
