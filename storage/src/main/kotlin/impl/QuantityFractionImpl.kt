package ru.it_arch.tools.samples.ribeye.storage.impl

import ru.it_arch.k3dm.ValueObject
import ru.it_arch.tools.samples.ribeye.storage.Quantity

@ConsistentCopyVisibility
public data class QuantityFractionImpl private constructor(
    override val numerator: Long,
    override val denominator: Long
) : Quantity.Fraction {
    init {
        validate()
    }

    override fun <T : ValueObject.Data> fork(vararg args: Any?): T {
        TODO("Not used")
    }

    public companion object {
        public operator fun invoke(numerator: Long, denominator: Long): Quantity.Fraction =
            QuantityFractionImpl(numerator, denominator)
    }
}
