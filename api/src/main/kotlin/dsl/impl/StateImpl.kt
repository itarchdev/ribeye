package ru.it_arch.tools.samples.ribeye.dsl.impl

import ru.it_arch.k3dm.ValueObject
import ru.it_arch.tools.samples.ribeye.dsl.Macronutrients
import ru.it_arch.tools.samples.ribeye.dsl.Op
import ru.it_arch.tools.samples.ribeye.dsl.Quantity
import ru.it_arch.tools.samples.ribeye.dsl.State
import ru.it_arch.tools.samples.ribeye.dsl.ValueChain
import kotlin.reflect.KClass
import kotlin.time.Duration

@ConsistentCopyVisibility
public data class StateImpl<out T : Op> private constructor(
    override val opType: KClass<out T>,
    override val macronutrients: Macronutrients,
    override val quantity: Quantity,
    override val elapsed: Duration,
    override val value: ValueChain
) : State<T> {
    
    init {
        validate()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <R : ValueObject.Data> fork(vararg args: Any?): R =
        Builder<T>().apply {

        }.build() as R

    public class Builder<T : Op> {
        public var opType: KClass<out T>? = null
        public var macronutrients: Macronutrients? = null
        public var quantity: Quantity? = null
        public var elapsed: Duration? = null
        public var value: ValueChain? = null

        public fun build(): State<T> {
            requireNotNull(opType) { "opType must be set" }
            requireNotNull(macronutrients) { "macronutrients must be set" }
            requireNotNull(quantity) { "quantity must be set" }
            requireNotNull(elapsed) { "elapsed must be set" }
            requireNotNull(value) { "value must be set" }

            return StateImpl(opType!!, macronutrients!!, quantity!!, elapsed!!, value!!)
        }
    }
}
