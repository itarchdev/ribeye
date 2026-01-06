package ru.it_arch.tools.samples.ribeye.data

import ru.it_arch.k3dm.ValueObject

public sealed interface Quantity {
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

        public fun isNotNegative(): Boolean =
            boxed >= 0
    }

    /**
     * Весовое измерение в минимальных единицах точности
     * */
    public interface Weight : ValueObject.Value<Long>, Quantity, Comparable<Weight> {
        override fun validate() {
            require(boxed >= 0) { "Quantity.Weight must be >= 0" }
        }

        override fun compareTo(other: Weight): Int =
            (boxed - other.boxed).let { if (it == 0L) 0 else if (it < 0) -1 else 1 }

        public operator fun minus(other: Weight): Weight =
            apply(boxed - other.boxed)

        public fun isNotNegative(): Boolean =
            boxed >= 0
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
    }
}
