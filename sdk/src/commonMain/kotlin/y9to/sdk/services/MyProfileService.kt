package y9to.sdk.services

import kotlinx.coroutines.flow.Flow
import y9to.api.types.MyProfile
import y9to.sdk.Client


interface MyProfileService {
    val myProfile: Flow<MyProfile?>
}

class MyProfileServiceDefault(private val client: Client) : MyProfileService {
    override val myProfile = client.user.myProfile
}
