package ru.it_arch.tools.samples.ribeye.storage.impl

import ru.it_arch.k3dm.ValueObject
import ru.it_arch.tools.samples.ribeye.data.Expiration
import ru.it_arch.tools.samples.ribeye.data.Macronutrients
import ru.it_arch.tools.samples.ribeye.data.Quantity
import ru.it_arch.tools.samples.ribeye.data.Resource
import kotlin.time.Instant

@ConsistentCopyVisibility
public data class RosemaryImpl private constructor(
    override val macronutrients: Macronutrients,
    override val quantity: Quantity.Piece,
    override val expiration: Expiration
) : Resource.Rosemary {

    init {
        validate()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ValueObject.Data> fork(vararg args: Any?): T =
        Builder().apply {
            macronutrients = args[0] as Macronutrients
            quantity = args[1] as Quantity.Piece
            expiration = args[2] as Expiration
        }.build() as T

    public class Builder {
        public var macronutrients: Macronutrients? = null
        public var quantity: Quantity.Piece? = null
        public var expiration: Expiration? = null

        public fun build(): Resource.Rosemary {
            requireNotNull(macronutrients) { "Rosemary.macronutrients must be set" }
            requireNotNull(quantity) { "Rosemary.quantity must be set" }
            requireNotNull(expiration) { "Rosemary.expiration must be set" }

            return RosemaryImpl(macronutrients!!, quantity!!, expiration!!)
        }
    }

    public class DslBuilder {
        public var macronutrients: Macronutrients? = null
        public var quantity: Int? = null
        public var expiration: Instant? = null

        public fun build(): Resource.Rosemary {
            requireNotNull(macronutrients) { "Rosemary.macronutrients must be set" }
            requireNotNull(quantity) { "Rosemary.quantity must be set" }
            requireNotNull(expiration) { "Rosemary.expiration must be set" }

            return RosemaryImpl(
                macronutrients!!,
                QuantityPieceImpl(quantity!!),
                ExpirationImpl(expiration!!)
            )
        }
    }
}
