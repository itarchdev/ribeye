package ru.it_arch.tools.samples.ribeye

import ru.it_arch.k3dm.ValueObject
import kotlin.reflect.KClass
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

public sealed interface Resource : ValueObject.Data {
    /** КБЖУ */
    public val macronutrients: Macronutrients

    /** Количество в разных ипостасях */
    public val quantity: Quantity

    /** Время истечения срока хранения */
    public val expiration: Expiration

    /** Максимальный срок хранения. Имманентная величина. */
    public val maxStorage: Duration

    /** Проверка просроченности ресурса */
    public val isRotten: Boolean
        get() = expiration.boxed < Clock.System.now()

    /** Мясо для стейка */
    public interface Meat : Resource {
        override val quantity: Quantity.Weight

        /** Максимальный срок хранения. Согласно СанПиН 2.3.2.1324-03. Приложение №1, п.1 */
        override val maxStorage: Duration
            get() = 48.hours

        override fun validate() {}
    }

    /** Условный гриль как ресурс. Для упрощения полагается на равне с пищевыми ресурсами. */
    public interface Grill : Resource {
        override val quantity: Quantity.Weight

        override val maxStorage: Duration
            get() = Duration.INFINITE

        override fun validate() {}
    }

    /** Компоненты для приготовления условного соуса. Для упрощения полагается как единый ресурс. */
    public interface SauceIngredients : Resource {
        override val quantity: Quantity.Weight

        override val maxStorage: Duration
            get() = 100.days

        override fun validate() {}
    }

    /** Розмарин */
    public interface Rosemary : Resource {
        override val quantity: Quantity.Piece

        override val maxStorage: Duration
            get() = 300.days

        override fun validate() {}
    }

    /** Готовый стейк */
    public interface Steak : Resource {
        override val quantity: Quantity.Weight

        override val maxStorage: Duration
            get() = 20.minutes

        override fun validate() {}
    }

    public enum class Types(public val type: KClass<out Resource>) {
        MEAT(Meat::class),
        GRILL(Grill::class),
        SAUCE(SauceIngredients::class),
        ROSEMARY(Rosemary::class),
        STEAK(Steak::class)
    }
}
