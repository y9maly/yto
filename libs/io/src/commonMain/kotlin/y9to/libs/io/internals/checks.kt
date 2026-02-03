package y9to.libs.io.internals


internal fun ByteArray.checkBounds(start: Int, size: Int) {
    require(start >= 0) { "start[$start] must be non-negative" }
    require(start <= this.size) { "start[$start] must be <= byteArray.size[$size]" }
    require(start + size <= this.size) { "(start[$start] + size[$size]) must be <= byteArray.size[$size]" }
}
