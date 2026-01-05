package ru.it_arch.tools.samples.ribeye.data

import ru.it_arch.k3dm.ValueObject

public sealed interface Quantity {
    /**
     * Штучное измерение в упаковках
     * */
    public interface Pack : ValueObject.Value<Int>, Quantity {
        override fun validate() {
            require(boxed >= 0) { "Quantity.Pack must be >= 0" }
        }
    }

    /**
     * Весовое измерение в минимальных единицах точности
     * */
    public interface Weight : ValueObject.Value<Long>, Quantity {
        override fun validate() {
            require(boxed >= 0) { "Quantity.Weight must be >= 0" }
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
    }
}
