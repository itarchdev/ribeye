package ru.it_arch.tools.samples.ribeye.storage.impl

import kotlinx.datetime.toInstant
import ru.it_arch.k3dm.ValueObject
import ru.it_arch.tools.samples.ribeye.storage.ResourceOld
import kotlin.time.Instant

@JvmInline
public value class ExpirationImpl private constructor(
    override val boxed: Instant
) : ResourceOld.Expiration {

    init {
        validate()
    }

    override fun <T : ValueObject.Value<Instant>> apply(boxed: Instant): T {
        TODO("Not used")
    }

    override fun toString(): String =
        ResourceOld.Expiration.format(boxed)

    public companion object {
        public operator fun invoke(value: Instant): ResourceOld.Expiration =
            ExpirationImpl(value)

        public fun parse(src: String): ResourceOld.Expiration =
            ResourceOld.Expiration.LOCAL_FORMAT.parse(src)
                .toInstant(ResourceOld.Expiration.LOCAL_TIME_ZONE)
                .let(::ExpirationImpl)
    }
}
