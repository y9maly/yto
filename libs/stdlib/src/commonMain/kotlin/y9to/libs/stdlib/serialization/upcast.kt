package y9to.libs.stdlib.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import y9to.libs.stdlib.multiplatform.qualifiedNameOrNull
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf


@Deprecated("Experimental. Be careful.")
inline fun <reified T : R, R> KSerializer<T>.upcastUnsafe(): KSerializer<R> {
    return upcastUnsafeImpl(typeOf<T>()) { it is T }
}

@PublishedApi
internal fun <T : R, R> KSerializer<T>.upcastUnsafeImpl(tType: KType, isT: (R) -> Boolean): KSerializer<R> {
    val tDisplayName = run {
        val klass = tType.classifier as? KClass<*>
        val name = klass?.qualifiedNameOrNull() ?: klass?.simpleName
        if (name != null) "'$name'"
        else "anonymous type"
    }

    return UpcastedKSerializer(this, tDisplayName, isT)
}

internal class UpcastedKSerializer<T : R, R>(
    private val underlying: KSerializer<T>,
    private val tDisplayName: String,
    private val isT: (R) -> Boolean,
) : KSerializer<R> {
    override val descriptor get() = underlying.descriptor

    @Suppress("UNCHECKED_CAST")
    override fun serialize(encoder: Encoder, value: R) {
        if (!isT(value))
            throw SerializationException("'$value' cannot be serialized as $tDisplayName")
        return underlying.serialize(encoder, value as T)
    }

    override fun deserialize(decoder: Decoder): R {
        return underlying.deserialize(decoder)
    }
}
