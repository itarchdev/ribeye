package ru.it_arch.tools.samples.ribeye.storage.impl

import ru.it_arch.k3dm.ValueObject
import ru.it_arch.tools.samples.ribeye.data.Quantity

@ConsistentCopyVisibility
public data class QuantityFractionImpl private constructor(
    override val numerator: Int,
    override val denominator: Int
) : Quantity.Fraction {
    init {
        validate()
    }

    override fun <T : ValueObject.Data> fork(vararg args: Any?): T {
        TODO("Not used")
    }

    public companion object {
        public operator fun invoke(numerator: Int, denominator: Int): Quantity.Fraction =
            QuantityFractionImpl(numerator, denominator)
    }
}
