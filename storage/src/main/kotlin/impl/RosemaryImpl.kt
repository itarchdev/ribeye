package ru.it_arch.tools.samples.ribeye.storage.impl

import ru.it_arch.k3dm.ValueObject
import ru.it_arch.tools.samples.ribeye.storage.Macronutrients
import ru.it_arch.tools.samples.ribeye.storage.ResourceOld
import kotlin.time.Instant

@ConsistentCopyVisibility
public data class RosemaryImpl private constructor(
    override val macronutrients: Macronutrients,
    override val expiration: ResourceOld.Expiration
) : ResourceOld.Rosemary {
    init {
        validate()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ValueObject.Data> fork(vararg args: Any?): T =
        Builder().apply {
            macronutrients = args[0] as Macronutrients
            expiration = args[1] as ResourceOld.Expiration
        }.build() as T

    public class Builder {
        public var macronutrients: Macronutrients? = null
        public var expiration: ResourceOld.Expiration? = null

        public fun build(): ResourceOld.Rosemary {
            requireNotNull(macronutrients) { "Rosemary.macronutrients must be set" }
            requireNotNull(expiration) { "Rosemary.expiration must be set" }

            return RosemaryImpl(macronutrients!!, expiration!!)
        }
    }

    public class DslBuilder {
        public var macronutrients: Macronutrients? = null
        public var expiration: Instant? = null

        public fun build(): ResourceOld.Rosemary {
            requireNotNull(macronutrients) { "Rosemary.macronutrients must be set" }
            requireNotNull(expiration) { "Rosemary.expiration must be set" }

            return RosemaryImpl(macronutrients!!, ExpirationImpl(expiration!!))
        }
    }
}
