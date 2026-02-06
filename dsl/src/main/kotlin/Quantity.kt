package ru.it_arch.tools.samples.ribeye

import ru.it_arch.k3dm.ValueObject
import java.math.BigDecimal
import java.math.RoundingMode

public sealed interface Quantity {

    /**
     * Изменение количества на долю (процент).
     *
     * @param value доля, на которую увеличивается исходное количество
     * @return итоговое значение
     * */
    public fun addPercent(value: Double): Quantity

    /**
     * Штучное измерение
     * */
    @JvmInline
    public value class Piece private constructor(
        override val boxed: Int
    ): ValueObject.Value<Int>, Quantity, Comparable<Piece> {

        init {
            validate()
        }

        override fun validate() {
            require(boxed >= 0) { "Quantity.Pack must be >= 0" }
        }

        override fun compareTo(other: Piece): Int =
            boxed - other.boxed

        public operator fun minus(other: Piece): Piece =
            apply(boxed - other.boxed)

        public operator fun plus(other: Piece): Piece =
            apply(boxed + other.boxed)

        public fun isNotNegative(): Boolean =
            boxed >= 0

        override fun addPercent(value: Double): Quantity {
            val base = BigDecimal(boxed.toLong())
                .multiply(BigDecimal.ONE + BigDecimal.valueOf(value))
                .setScale(0, RoundingMode.HALF_UP)
                .toInt()
            return apply(base)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T : ValueObject.Value<Int>> apply(boxed: Int): T =
            Piece(boxed) as T

        override fun toString(): String =
            boxed.toString()

        public companion object Companion {
            public operator fun invoke(value: Int): Piece =
                Piece(value)
        }
    }

    /**
     * Весовое измерение в минимальных единицах точности
     * */
    @JvmInline
    public value class Weight(
        override val boxed: Long
    ) : ValueObject.Value<Long>, Quantity, Comparable<Weight> {

        init {
            validate()
        }

        override fun validate() {
            require(boxed >= 0) { "Quantity.Weight must be >= 0" }
        }

        override fun compareTo(other: Weight): Int =
            boxed.compareTo(other.boxed)

        public operator fun plus(other: Weight): Weight =
            apply(boxed + other.boxed)

        public operator fun minus(other: Weight): Weight =
            apply(boxed - other.boxed)

        public fun isNotNegative(): Boolean =
            boxed >= 0

        override fun addPercent(value: Double): Quantity {
            val base = BigDecimal(boxed)
                .multiply(BigDecimal.ONE + BigDecimal.valueOf(value))
                .setScale(0, RoundingMode.HALF_UP)
                .toLong()
            return apply(base)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T : ValueObject.Value<Long>> apply(boxed: Long): T =
            Weight(boxed) as T

        override fun toString(): String =
            boxed.toString()

        public companion object {
            public operator fun invoke(value: Long): Weight =
                Weight(value)
        }
    }

    /**
     * Долевое измерение в рациональных числах (треть, половина и т.п.)
     * */
    @ConsistentCopyVisibility
    public data class Fraction private constructor(
        public val numerator: Int,
        public val denominator: Int
    ): ValueObject.Data, Quantity {

        init {
            validate()
        }

        override fun validate() {
            require(numerator >= 0 && denominator > 0) { "Quantity.Fraction must be rational non-zero number" }
        }

        override fun addPercent(value: Double): Quantity {
            TODO("Not yet implemented")
        }

        override fun <T : ValueObject.Data> fork(vararg args: Any?): T {
            TODO("Not used")
        }

        public companion object {
            public operator fun invoke(numerator: Int, denominator: Int): Fraction =
                Fraction(numerator, denominator)
        }
    }
}
