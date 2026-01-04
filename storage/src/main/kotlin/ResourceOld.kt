package ru.it_arch.tools.samples.ribeye.storage

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import ru.it_arch.k3dm.ValueObject
import kotlin.reflect.KClass
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

public sealed interface ResourceOld : ValueObject.Data {
    /** КБЖУ */
    public val macronutrients: Macronutrients
    //public val quantity: ???
    /** Время истечения срока хранения */
    public val expiration: ru.it_arch.tools.samples.ribeye.storage.Resource.Expiration

    public interface Meat : ResourceOld {
        override fun validate() {
            Clock.System.now().also { now ->
                (now - expiration.boxed).also { elapsed ->
                    require(expiration.boxed > now && elapsed <= MAX_STORAGE_DURATION)
                    { "Meat has expired. Current duration $elapsed is over $MAX_STORAGE_DURATION" }
                }
            }
        }

        public companion object Companion {
            // Согласно СанПиН 2.3.2.1324-03. Приложение №1, п.1
            public val MAX_STORAGE_DURATION: Duration = 48.hours
        }
    }

    public interface Grill : ResourceOld {
        override fun validate() {}
    }

    public interface SauceIngredients : ResourceOld {
        override fun validate() {
            Clock.System.now().also { now ->
                (now - expiration.boxed).also { elapsed ->
                    require(expiration.boxed > now && elapsed <= MAX_STORAGE_DURATION)
                    { "SauceIngredients has expired. Current duration $elapsed is over $MAX_STORAGE_DURATION" }
                }
            }
        }

        public companion object Companion {
            public val MAX_STORAGE_DURATION: Duration = 100.days
        }
    }

    public interface Rosemary : ResourceOld {
        override fun validate() {
            Clock.System.now().also { now ->
                (now - expiration.boxed).also { elapsed ->
                    require(expiration.boxed > now && elapsed <= MAX_STORAGE_DURATION)
                    { "Rosemary has expired. Current duration $elapsed is over $MAX_STORAGE_DURATION" }
                }
            }
        }

        public companion object Companion {
            public val MAX_STORAGE_DURATION: Duration = 300.days
        }
    }

    /**
     * Срок хранения
     * */
    public interface Expiration : ValueObject.Value<Instant>, Comparable<ru.it_arch.tools.samples.ribeye.storage.Resource.Expiration> {
        override fun validate() {
            require(boxed > Clock.System.now()) { "Expiration: ${format(boxed)} must be later than the current time ${format(Clock.System.now())}" }
        }

        override fun compareTo(other: ru.it_arch.tools.samples.ribeye.storage.Resource.Expiration): Int =
            boxed.compareTo(other.boxed)

        public companion object Companion {
            public val LOCAL_FORMAT: DateTimeFormat<LocalDateTime> = LocalDateTime.Format {
                day(); char('.'); monthNumber(); char('.'); year()
                char(' ')
                hour(); char(':'); minute(); char(':'); second()
            }

            public val LOCAL_TIME_ZONE: TimeZone = TimeZone.currentSystemDefault()

            public fun format(instant: Instant): String =
                LOCAL_TIME_ZONE.let(instant::toLocalDateTime)
                    .let(LOCAL_FORMAT::format)
        }
    }

    public companion object Companion {
        public val types: Set<KClass<out ResourceOld>> = setOf(
            Meat::class,
            Grill::class,
            SauceIngredients::class,
            Rosemary::class
        )
    }
}
