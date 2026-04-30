package y9to.sdk.services

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import y9to.api.types.EditMeError
import y9to.api.types.FileId
import y9to.common.types.Birthday
import y9to.libs.stdlib.Union
import y9to.libs.stdlib.optional.Optional
import y9to.libs.stdlib.optional.none
import y9to.sdk.Client
import y9to.sdk.types.EditProfileProperties
import kotlin.time.Clock


interface EditMyProfileService {
    val editProfileProperties: Flow<EditProfileProperties>

    suspend fun edit(
        firstName: Optional<String> = none(),
        lastName: Optional<String?> = none(),
        bio: Optional<String?> = none(),
        birthday: Optional<Birthday?> = none(),
        cover: Optional<FileId?> = none(),
        avatar: Optional<FileId?> = none(),
    ): Union<Unit, EditMeError>
}

class EditMyProfileServiceDefault(
    private val clock: Clock,
    private val timeZone: StateFlow<TimeZone>,
    private val client: Client
) : EditMyProfileService {
    override val editProfileProperties = flowOf(EditProfileProperties(
        maxCoverFileSize = null,
        maxAvatarFileSize = null,
        firstNameLength = 1..64,
        lastNameLength = 1..64,
        bioLength = 1..1024,
        birthdayYearRange = 1800..clock.now().toLocalDateTime(timeZone.value).year,
        firstNameCanBeNull = false,
        lastNameCanBeNull = true,
        bioCanBeNull = true,
    ))

    override suspend fun edit(
        firstName: Optional<String>,
        lastName: Optional<String?>,
        bio: Optional<String?>,
        birthday: Optional<Birthday?>,
        cover: Optional<FileId?>,
        avatar: Optional<FileId?>
    ): Union<Unit, EditMeError> {
        return client.user.editMe(
            firstName = firstName,
            lastName = lastName,
            bio = bio,
            birthday = birthday,
            cover = cover,
            avatar = avatar,
        )
    }
}
