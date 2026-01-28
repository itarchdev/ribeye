package ru.it_arch.tools.samples.ribeye.dsl

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
public interface State<out T : Op> : ValueObject.Data {
    public val opType: KClass<out T>
    public val macronutrients: Macronutrients
    public val quantity: Quantity
    public val elapsed: Duration
    public val value: ValueChain

    override fun validate() {}
}

public typealias SteakReady = State<Op.Finish>
