package ru.it_arch.tools.samples.ribeye

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import ru.it_arch.k3dm.ValueObject
import kotlin.time.Instant

/**
 * Срок хранения
 * */
@JvmInline
public value class Expiration private constructor(
    override val boxed: Instant
): ValueObject.Value<Instant>, Comparable<Expiration> {
    public val localFormat: String
        get() = format(boxed)

    init {
        validate()
    }

    override fun validate() {
        //require(boxed > Clock.System.now()) { "Expiration: $localFormat must be later than the current time ${format(Clock.System.now())}" }
    }

    override fun compareTo(other: Expiration): Int =
        boxed.compareTo(other.boxed)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ValueObject.Value<Instant>> apply(boxed: Instant): T =
        Expiration(boxed) as T

    override fun toString(): String =
        format(boxed)

    public companion object Companion {
        public val LOCAL_FORMAT: DateTimeFormat<LocalDateTime> = LocalDateTime.Format {
            day(); char('.'); monthNumber(); char('.'); year()
            char(' ')
            hour(); char(':'); minute(); char(':'); second()
        }

        public val LOCAL_TIME_ZONE: TimeZone = TimeZone.currentSystemDefault()

        public fun format(instant: Instant): String =
            LOCAL_TIME_ZONE.let(instant::toLocalDateTime).let(LOCAL_FORMAT::format)

        public operator fun invoke(value: Instant): Expiration =
            Expiration(value)

        public fun parse(src: String): Expiration =
            LOCAL_FORMAT.parse(src)
                .toInstant(LOCAL_TIME_ZONE)
                .let(::Expiration)
    }
}
