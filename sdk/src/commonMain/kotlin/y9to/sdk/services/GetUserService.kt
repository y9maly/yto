package y9to.sdk.services

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import y9to.api.types.InputUser
import y9to.api.types.User
import y9to.sdk.Client


interface GetUserService {
    fun getFlow(input: InputUser): Flow<User?>

    suspend fun get(input: InputUser) = getFlow(input).first()
}


class GetUserServiceDefault(private val client: Client) : GetUserService {
    override fun getFlow(input: InputUser) = client.user.getFlow(input)
    override suspend fun get(input: InputUser) = client.user.get(input)
}
