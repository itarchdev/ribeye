package ru.it_arch.tools.samples.ribeye.storage.impl

import ru.it_arch.k3dm.ValueObject
import ru.it_arch.tools.samples.ribeye.storage.Macronutrients
import ru.it_arch.tools.samples.ribeye.storage.ResourceOld
import kotlin.time.Instant

@ConsistentCopyVisibility
public data class GrillImpl private constructor(
    override val macronutrients: Macronutrients,
    override val expiration: ResourceOld.Expiration
) : ResourceOld.Grill {

    init {
        validate()
    }

    override fun <T : ValueObject.Data> fork(vararg args: Any?): T {
        TODO("Not used")
    }

    public companion object {
        public val DEFAULT: ResourceOld.Grill = GrillImpl(
            MacronutrientsImpl.DEFAULT,
            ExpirationImpl(Instant.DISTANT_FUTURE)
        )
    }
}
