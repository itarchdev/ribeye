package ru.it_arch.tools.samples.ribeye.dsl.impl

import ru.it_arch.k3dm.ValueObject
import ru.it_arch.tools.samples.ribeye.dsl.Price
import ru.it_arch.tools.samples.ribeye.dsl.ResourceState
import ru.it_arch.tools.samples.ribeye.storage.Macronutrients
import ru.it_arch.tools.samples.ribeye.storage.Quantity
import ru.it_arch.tools.samples.ribeye.storage.impl.MacronutrientsImpl
import kotlin.time.Duration

@ConsistentCopyVisibility
public data class GrillImpl private constructor(
    override val macronutrients: Macronutrients,
    override val quantity: Quantity,
    override val price: Price,
    override val elapsed: Duration,
    override val state: ResourceState.Grill.State
) : ResourceState.Grill {

    init {
        validate()
    }

    override fun <T : ValueObject.Data> fork(vararg args: Any?): T {
        TODO("Not used")
    }

    override fun setState(state: ResourceState.Grill.State): ResourceState.Grill =
        copy(state = state)

    public class Builder {
        public var macronutrients: Macronutrients = MacronutrientsImpl.DEFAULT
        public var quantity: Quantity? = null
        public var price: Price? = null
        public var elapsed: Duration = Duration.ZERO
        public var state: ResourceState.Grill.State = ResourceState.Grill.State.INIT

        public fun build(): ResourceState.Grill {
            requireNotNull(quantity) { "Grill.quantity must be set " }
            requireNotNull(price) { "Grill.price must be set " }

            return GrillImpl(macronutrients, quantity!!, price!!, elapsed, state)
        }
    }
}
