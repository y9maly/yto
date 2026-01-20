package y9to.libs.stdlib


object EmptyIterator : Iterator<Nothing> {
    override fun next() = throw NoSuchElementException("EmptyIterator is always empty")
    override fun hasNext() = false
}
