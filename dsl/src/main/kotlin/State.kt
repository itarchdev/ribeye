package ru.it_arch.tools.samples.ribeye

import ru.it_arch.k3dm.ValueObject
import kotlin.reflect.KClass
import kotlin.time.Duration

/**
 * Состояние операции на выходе
 *
 * @param T тип операции
 * @param opType [KClass] типа операции
 * @param macronutrients КБЖУ
 * @param quantity количество
 * @param elapsed затраченное время
 * @param value цепочка ценности
 * */

@ConsistentCopyVisibility
public data class State<out T : Op> private constructor(
    public val opType: KClass<out T>,
    public val macronutrients: Macronutrients,
    public val quantity: Quantity,
    public val elapsed: Duration,
    public val value: ValueChain
) : ValueObject.Data {

    init {
        validate()
    }

    override fun validate() {}

    @Suppress("UNCHECKED_CAST")
    override fun <R : ValueObject.Data> fork(vararg args: Any?): R =
        Builder<T>().apply {
            opType = args[0] as KClass<out T>
            macronutrients = args[1] as Macronutrients
            quantity = args[2] as Quantity
            elapsed = args[3] as Duration
            value = args[4] as ValueChain
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

            return State(opType!!, macronutrients!!, quantity!!, elapsed!!, value!!)
        }
    }
}

public typealias SteakReady = State<Op.Finish>
