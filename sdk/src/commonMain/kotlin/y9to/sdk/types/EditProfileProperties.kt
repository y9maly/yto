package y9to.sdk.types


data class EditProfileProperties(
    val maxCoverFileSize: Long?,     // in bytes; null - unknown, unlimited
    val maxAvatarFileSize: Long?,    // in bytes; null - unknown, unlimited
    val firstNameLength: IntRange,   // 1..64
    val lastNameLength: IntRange,    // 1..64
    val bioLength: IntRange,         // 1..512
    val birthdayYearRange: IntRange, // 1900..<currentYear>
    val firstNameCanBeNull: Boolean, // false
    val lastNameCanBeNull: Boolean,  // true
    val bioCanBeNull: Boolean,       // true
)
