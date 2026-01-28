package ru.it_arch.tools.samples.ribeye.dsl.impl

import ru.it_arch.k3dm.ValueObject
import ru.it_arch.tools.samples.ribeye.dsl.ValueChain
import java.math.BigDecimal

@JvmInline
public value class ValueChainImpl private constructor(
    override val boxed: BigDecimal
) : ValueChain {

    init {
        validate()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ValueObject.Value<BigDecimal>> apply(boxed: BigDecimal): T =
        ValueChainImpl(boxed) as T

    override fun toString(): String =
        boxed.toString()

    public companion object {
        public operator fun invoke(value: BigDecimal): ValueChain =
            ValueChainImpl(value)

        public fun parse(src: String): ValueChain =
            ValueChainImpl(BigDecimal(src))
    }
}
