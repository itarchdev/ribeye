package ru.it_arch.tools.samples.ribeye.storage.impl

import ru.it_arch.k3dm.ValueObject
import ru.it_arch.tools.samples.ribeye.storage.Quantity

@JvmInline
public value class QuantityCountImpl private constructor(
    override val boxed: Long
) : Quantity.Count {

    init {
        validate()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ValueObject.Value<Long>> apply(boxed: Long): T =
        QuantityCountImpl(boxed) as T

    override fun toString(): String =
        boxed.toString()

    public companion object {
        public operator fun invoke(value: Long): Quantity.Count =
            QuantityCountImpl(value)
    }
}
