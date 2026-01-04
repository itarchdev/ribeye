package ru.it_arch.tools.samples.ribeye.dsl.impl

import ru.it_arch.k3dm.ValueObject
import ru.it_arch.tools.samples.ribeye.dsl.Price

@JvmInline
public value class PriceImpl private constructor(
    override val boxed: Long
) : Price {

    init {
        validate()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ValueObject.Value<Long>> apply(boxed: Long): T =
        PriceImpl(boxed) as T

    override fun toString(): String =
        boxed.toString()

    public companion object {
        public operator fun invoke(value: Long): Price =
            PriceImpl(value)
    }
}
