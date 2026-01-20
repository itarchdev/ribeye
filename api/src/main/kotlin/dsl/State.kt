package ru.it_arch.tools.samples.ribeye.dsl

import ru.it_arch.tools.samples.ribeye.data.Macronutrients
import ru.it_arch.tools.samples.ribeye.data.Quantity
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
 * @param valueChain добавленная стоимость
 * */
public data class State<out T : Op>(
    public val opType: KClass<out T>,
    public val macronutrients: Macronutrients,
    public val quantity: Quantity,
    public val elapsed: Duration,
    public val valueChain: ValueChain
)

public typealias SteakReady = State<Op.Finish>
