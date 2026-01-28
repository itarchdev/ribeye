package ru.it_arch.tools.samples.ribeye.dsl.impl

import ru.it_arch.k3dm.ValueObject
import ru.it_arch.tools.samples.ribeye.dsl.Quantity

@JvmInline
public value class QuantityWeightImpl private constructor(
    override val boxed: Long
) : Quantity.Weight {
    init {
        validate()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ValueObject.Value<Long>> apply(boxed: Long): T =
        QuantityWeightImpl(boxed) as T

    override fun toString(): String =
        boxed.toString()

    public companion object {
        public operator fun invoke(value: Long): Quantity.Weight =
            QuantityWeightImpl(value)
    }
}
