import y9to.sdk.KVStorage


class KVStorageInMemory : KVStorage {
    private val booleans = mutableMapOf<String, Boolean>()
    private val bytes = mutableMapOf<String, Byte>()
    private val shorts = mutableMapOf<String, Short>()
    private val ints = mutableMapOf<String, Int>()
    private val longs = mutableMapOf<String, Long>()
    private val strings = mutableMapOf<String, String>()

    override suspend fun put(key: String, value: Boolean) {
        booleans[key] = value
    }

    override suspend fun put(key: String, value: Byte) {
        bytes[key] = value
    }

    override suspend fun put(key: String, value: Short) {
        shorts[key] = value
    }

    override suspend fun put(key: String, value: Int) {
        ints[key] = value
    }

    override suspend fun put(key: String, value: Long) {
        longs[key] = value
    }

    override suspend fun put(key: String, value: String) {
        strings[key] = value
    }

    override suspend fun getBoolean(key: String): Boolean? {
        return booleans[key]
    }

    override suspend fun getByte(key: String): Byte? {
        return bytes[key]
    }

    override suspend fun getShort(key: String): Short? {
        return shorts[key]
    }

    override suspend fun getInt(key: String): Int? {
        return ints[key]
    }

    override suspend fun getLong(key: String): Long? {
        return longs[key]
    }

    override suspend fun getString(key: String): String? {
        return strings[key]
    }

    override suspend fun deleteBoolean(key: String) {
        booleans.remove(key)
    }

    override suspend fun deleteByte(key: String) {
        bytes.remove(key)
    }

    override suspend fun deleteShort(key: String) {
        shorts.remove(key)
    }

    override suspend fun deleteInt(key: String) {
        ints.remove(key)
    }

    override suspend fun deleteLong(key: String) {
        longs.remove(key)
    }

    override suspend fun deleteString(key: String) {
        strings.remove(key)
    }
}
