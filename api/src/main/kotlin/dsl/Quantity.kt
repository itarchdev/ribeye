package ru.it_arch.tools.samples.ribeye.dsl

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
    public interface Piece : ValueObject.Value<Int>, Quantity, Comparable<Piece> {
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
    }

    /**
     * Весовое измерение в минимальных единицах точности
     * */
    public interface Weight : ValueObject.Value<Long>, Quantity, Comparable<Weight> {
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
    }

    /**
     * Долевое измерение в рациональных числах (треть, половина и т.п.)
     * */
    public interface Fraction : ValueObject.Data, Quantity {
        public val numerator: Int
        public val denominator: Int

        override fun validate() {
            require(numerator >= 0 && denominator > 0) { "Quantity.Fraction must be rational non-zero number" }
        }

        override fun addPercent(value: Double): Quantity {
            TODO("Not yet implemented")
        }
    }
}
