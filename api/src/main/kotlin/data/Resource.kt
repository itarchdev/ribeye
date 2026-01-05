package ru.it_arch.tools.samples.ribeye.data

import ru.it_arch.k3dm.ValueObject
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

public sealed interface Resource : ValueObject.Data {
    /** КБЖУ */
    public val macronutrients: Macronutrients
    /** Количество */
    public val quantity: Quantity
    /** Время истечения срока хранения */
    public val expiration: Expiration

    /** Мясо для стейка */
    public interface Meat : Resource {
        override fun validate() {
            // Проверка просроченности хранения
            Clock.System.now().also { now ->
                (now - expiration.boxed).also { elapsed ->
                    require(expiration.boxed > now && elapsed <= MAX_STORAGE_DURATION)
                    { "Meat is rotten. Current duration $elapsed is over $MAX_STORAGE_DURATION" }
                }
            }
        }

        public companion object Companion {
            /** Максимальный срок хранения. Согласно СанПиН 2.3.2.1324-03. Приложение №1, п.1 */
            public val MAX_STORAGE_DURATION: Duration = 48.hours
        }
    }

    /** Условный гриль как ресурс. Для упрощения полагается как пищевой ресурс. */
    public interface Grill : Resource {
        override fun validate() {}
    }

    /** Компоненты для приготовления условного соуса. Для упрощения полагается как единый ресурс. */
    public interface SauceIngredients : Resource {
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

    /** Розмарин */
    public interface Rosemary : Resource {
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

}
