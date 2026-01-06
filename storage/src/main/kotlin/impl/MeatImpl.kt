package ru.it_arch.tools.samples.ribeye.storage.impl

import ru.it_arch.k3dm.ValueObject
import ru.it_arch.tools.samples.ribeye.data.Expiration
import ru.it_arch.tools.samples.ribeye.data.Macronutrients
import ru.it_arch.tools.samples.ribeye.data.Quantity
import ru.it_arch.tools.samples.ribeye.data.Resource
import kotlin.time.Instant

@ConsistentCopyVisibility
public data class MeatImpl private constructor(
    override val macronutrients: Macronutrients,
    override val quantity: Quantity.Weight,
    override val expiration: Expiration
) : Resource.Meat {

    init {
        validate()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ValueObject.Data> fork(vararg args: Any?): T =
        Builder().apply {
            macronutrients = args[0] as Macronutrients
            quantity = args[1] as Quantity.Weight
            expiration = args[2] as Expiration
        }.build() as T

    public class Builder {
        public var macronutrients: Macronutrients? = null
        public var quantity: Quantity.Weight? = null
        public var expiration: Expiration? = null

        public fun build(): Resource.Meat {
            requireNotNull(macronutrients) { "Meat.macronutrients must be set" }
            requireNotNull(quantity) { "Meat.quantity must be set" }
            requireNotNull(expiration) { "Meat.expiration must be set" }

            return MeatImpl(macronutrients!!, quantity!!, expiration!!)
        }
    }

    public class DslBuilder {
        public var macronutrients: Macronutrients? = null
        public var quantity: Long? = null
        public var expiration: Instant? = null

        public fun build(): Resource.Meat {
            requireNotNull(macronutrients) { "Meat.macronutrients must be set" }
            requireNotNull(quantity) { "Meat.quantity must be set" }
            requireNotNull(expiration) { "Meat.expiration must be set" }

            return MeatImpl(
                macronutrients!!,
                QuantityWeightImpl(quantity!!),
                ExpirationImpl(expiration!!)
            )
        }
    }
}
