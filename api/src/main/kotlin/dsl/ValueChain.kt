package ru.it_arch.tools.samples.ribeye.dsl

import ru.it_arch.k3dm.ValueObject
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Добавленная стоимость в условных единицах учета.
 * */
public interface ValueChain : ValueObject.Value<BigDecimal>, Comparable<ValueChain> {
    override fun validate() {
        require(boxed >= BigDecimal.ZERO) { "Value Chain must be >= 0" }
    }

    override fun compareTo(other: ValueChain): Int =
        boxed.compareTo(other.boxed)

    public operator fun plus(other: ValueChain): ValueChain =
        apply(boxed + other.boxed)

    public operator fun minus(other: ValueChain): ValueChain =
        apply(boxed - other.boxed)

    public operator fun times(other: ValueChain): ValueChain =
        apply(boxed * other.boxed)

    public operator fun div(other: ValueChain): ValueChain =
        apply(boxed.divide(other.boxed, CALC_SCALE, RoundingMode.HALF_UP))


    public companion object {
        public const val SCALE: Int = 2
        public const val CALC_SCALE: Int = 4
    }
}
