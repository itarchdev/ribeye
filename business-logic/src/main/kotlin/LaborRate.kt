package ru.it_arch.tools.samples.ribeye.bl

import ru.it_arch.k3dm.ValueObject
import java.math.BigDecimal

/**
 * Базовая ставка оплаты труда в час
 * */
@JvmInline
public value class LaborRate private constructor(
    override val boxed: BigDecimal
) : ValueObject.Value<BigDecimal>, Comparable<LaborRate> {

    init {
        validate()
    }

    override fun validate() {
        require(boxed in BigDecimal.ONE..2_500.toBigDecimal()) { "LaborRate must be in range 1..2500" }
    }

    override fun toString(): String =
        boxed.toString()

    override fun compareTo(other: LaborRate): Int =
        boxed.compareTo(other.boxed)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ValueObject.Value<BigDecimal>> apply(boxed: BigDecimal): T =
        LaborRate(boxed) as T

    public companion object {

        public const val SCALE: Int = 2
        public const val CALC_SCALE: Int = 6

        public operator fun invoke(value: BigDecimal): LaborRate =
            LaborRate(value)

        /*
        public fun parse(src: String): LaborRate =
            LaborRate(BigDecimal(src))*/
    }
}
