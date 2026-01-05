package ru.it_arch.tools.samples.ribeye.storage.impl

import ru.it_arch.k3dm.ValueObject
import ru.it_arch.tools.samples.ribeye.data.Quantity

@JvmInline
public value class QuantityPackImpl private constructor(
    override val boxed: Int
) : Quantity.Pack {

    init {
        validate()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ValueObject.Value<Int>> apply(boxed: Int): T =
        QuantityPackImpl(boxed) as T

    override fun toString(): String =
        boxed.toString()

    public companion object Companion {
        public operator fun invoke(value: Int): Quantity.Pack =
            QuantityPackImpl(value)
    }
}
