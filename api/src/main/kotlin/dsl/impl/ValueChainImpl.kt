package ru.it_arch.tools.samples.ribeye.dsl.impl

import ru.it_arch.k3dm.ValueObject
import ru.it_arch.tools.samples.ribeye.dsl.ValueChain

@JvmInline
public value class ValueChainImpl private constructor(
    override val boxed: Long
) : ValueChain {

    init {
        validate()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ValueObject.Value<Long>> apply(boxed: Long): T =
        ValueChainImpl(boxed) as T

    override fun toString(): String =
        boxed.toString()

    public companion object {
        public operator fun invoke(value: Long): ValueChain =
            ValueChainImpl(value)
    }
}
