package ru.it_arch.tools.samples.ribeye.dsl.impl

import kotlinx.datetime.toInstant
import ru.it_arch.k3dm.ValueObject
import ru.it_arch.tools.samples.ribeye.dsl.Expiration
import kotlin.time.Instant

@JvmInline
public value class ExpirationImpl private constructor(
    override val boxed: Instant
) : Expiration {

    init {
        validate()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ValueObject.Value<Instant>> apply(boxed: Instant): T =
        ExpirationImpl(boxed) as T

    override fun toString(): String =
        Expiration.format(boxed)

    public companion object {
        public operator fun invoke(value: Instant): Expiration =
            ExpirationImpl(value)

        public fun parse(src: String): Expiration =
            Expiration.LOCAL_FORMAT.parse(src)
                .toInstant(Expiration.LOCAL_TIME_ZONE)
                .let(::ExpirationImpl)
    }
}
