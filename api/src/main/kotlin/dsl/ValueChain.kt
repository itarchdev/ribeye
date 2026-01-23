package ru.it_arch.tools.samples.ribeye.dsl

import ru.it_arch.k3dm.ValueObject

/**
 * Добавленная стоимость в минимальных единицах учета — копейки, центы, сатоши, внутренняя валюта.
 * */
public interface ValueChain : ValueObject.Value<Long>, Comparable<ValueChain> {
    override fun validate() {
        require(boxed >= 0) { "Value Chain must be >= 0" }
    }

    override fun compareTo(other: ValueChain): Int =
        boxed.compareTo(other.boxed)

    public operator fun plus(other: ValueChain): ValueChain =
        apply(boxed + other.boxed)

    public operator fun minus(other: ValueChain): ValueChain =
        apply(boxed - other.boxed)

    public operator fun times(other: ValueChain): ValueChain =
        apply(boxed * other.boxed)
}
