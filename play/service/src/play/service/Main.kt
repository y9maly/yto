package play.service

import backend.core.reference.PostReference
import backend.core.reference.UserReference
import backend.core.types.PostId


suspend fun main() {
    println(service.user.get(UserReference.Random))
    println(service.post.get(PostReference.Random))
    println(service.post.get(PostId(10)))
}
