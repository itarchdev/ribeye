package ru.it_arch.tools.samples.ribeye.dsl.impl

import ru.it_arch.k3dm.ValueObject
import ru.it_arch.tools.samples.ribeye.dsl.Quantity

@JvmInline
public value class QuantityPieceImpl private constructor(
    override val boxed: Int
) : Quantity.Piece {

    init {
        validate()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ValueObject.Value<Int>> apply(boxed: Int): T =
        QuantityPieceImpl(boxed) as T

    override fun toString(): String =
        boxed.toString()

    public companion object Companion {
        public operator fun invoke(value: Int): Quantity.Piece =
            QuantityPieceImpl(value)
    }
}
