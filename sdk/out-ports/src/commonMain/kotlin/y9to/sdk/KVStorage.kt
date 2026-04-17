package y9to.sdk


interface KVStorage {
    suspend fun put(key: String, value: Boolean)
    suspend fun put(key: String, value: Byte)
    suspend fun put(key: String, value: Short)
    suspend fun put(key: String, value: Int)
    suspend fun put(key: String, value: Long)
    suspend fun put(key: String, value: String)

    suspend fun getBoolean(key: String): Boolean?
    suspend fun getByte(key: String): Byte?
    suspend fun getShort(key: String): Short?
    suspend fun getInt(key: String): Int?
    suspend fun getLong(key: String): Long?
    suspend fun getString(key: String): String?

    suspend fun deleteBoolean(key: String)
    suspend fun deleteByte(key: String)
    suspend fun deleteShort(key: String)
    suspend fun deleteInt(key: String)
    suspend fun deleteLong(key: String)
    suspend fun deleteString(key: String)
}
