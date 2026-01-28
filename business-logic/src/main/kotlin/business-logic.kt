package ru.it_arch.tools.samples.ribeye.bl

import ru.it_arch.tools.samples.ribeye.dsl.Resource
import ru.it_arch.tools.samples.ribeye.dsl.Op
import ru.it_arch.tools.samples.ribeye.dsl.State
import ru.it_arch.tools.samples.ribeye.dsl.ValueChain
import ru.it_arch.tools.samples.ribeye.dsl.impl.ValueChainImpl
import ru.it_arch.tools.samples.ribeye.dsl.impl.checkGrillState
import ru.it_arch.tools.samples.ribeye.dsl.impl.checkMeatState
import ru.it_arch.tools.samples.ribeye.dsl.impl.fats
import ru.it_arch.tools.samples.ribeye.dsl.impl.getGrillState
import ru.it_arch.tools.samples.ribeye.dsl.impl.getMeatState
import ru.it_arch.tools.samples.ribeye.dsl.impl.marinateState
import ru.it_arch.tools.samples.ribeye.dsl.impl.weight
import ru.it_arch.tools.samples.ribeye.dsl.impl.valueChain
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes


/** Базовая ставка у.е. в час */
public val laborRatePerHour: LaborRate = 1_300.laborRate

/**
 * Простой расчет добавленной стоимости из затраченного времени, базовой ставки и коэффициентов операции.
 * (laborRate * t) * (1 + [overheadCostsFactor]) + [fixedOverhead]
 *
 * @receiver затраченное время
 * @param overheadCostsFactor коэффициент накладных расходов
 * @param fixedOverhead постоянные накладные расходы
 * @return добавленная стоимость
 * */
public fun Duration.simpleValueChain(overheadCostsFactor: Double, fixedOverhead: ValueChain): ValueChain {
    val elapsedInSeconds = BigDecimal.valueOf(inWholeSeconds)
    val laborRateInSeconds = laborRatePerHour.boxed.divide(SECONDS_IN_HOUR, LaborRate.CALC_SCALE, RoundingMode.HALF_UP)
    val cost = laborRateInSeconds * elapsedInSeconds
    val multiplier = BigDecimal.ONE + BigDecimal.valueOf(overheadCostsFactor)
    val totalRaw = cost * multiplier + fixedOverhead.boxed
    return ValueChainImpl(totalRaw.setScale(LaborRate.SCALE, RoundingMode.HALF_UP))
}


/**
 * Бизнес-правила приемки мяса из хранилища и изменение состояния.
 *
 * @receiver исходный ресурс
 * @param elapsed затраченное операцией время
 * @return состояние [State] принятого для готовки мяса
 * */
public fun Resource.Meat.stateForGetMeat(elapsed: Duration): State<Op.Meat.Get> {
    require(!isRotten) { "Meat is rotten. Expired at: $expiration" }
    require(quantity in 350.weight..450.weight) { "Meat acceptance: weight must be in range [350..450g]" }
    require(elapsed in 0.5.minutes..3.minutes) { "Meat acceptance: duration must be in range [0.5..3 min]" }

    return getMeatState {
        this.opType = Op.Meat.Get::class
        this.macronutrients = this@stateForGetMeat.macronutrients
        this.quantity = this@stateForGetMeat.quantity
        this.elapsed = elapsed
        this.value = elapsed.simpleValueChain(0.15, 20.valueChain)
    }
}

/**
 * Бизнес-правила проверки принятого мяса и изменение состояния.
 *
 * @receiver состояние принятого мяса
 * @param elapsed затраченное операцией время
 * @return состояние [State] проверенного мяса
 * */
public fun State<Op.Meat.Get>.stateForCheckMeat(elapsed: Duration): State<Op.Meat.Check> {
    // Полагаем стейк рибай жирностью 18-25%
    require(macronutrients.fats in 18.0.fats..25.0.fats) { "Meat check: fats must be in range [18..25g]" }
    require(elapsed <= 5.minutes) { "Meat check: duration must be <= 5 min" }

    return checkMeatState {
        this.opType = Op.Meat.Check::class
        this.macronutrients = this@stateForCheckMeat.macronutrients
        this.quantity = this@stateForCheckMeat.quantity
        this.elapsed = this@stateForCheckMeat.elapsed + elapsed
        this.value = this@stateForCheckMeat.value + elapsed.simpleValueChain(0.2, 25.valueChain)
    }
}

/**
 * Бизнес-правила маринования мяса и изменение состояния.
 *
 * @receiver состояние проверенного мяса
 * @param elapsed затраченное операцией время
 * @return состояние [State] маринованого мяса
 * */
public fun State<Op.Meat.Check>.stateForMarinate(elapsed: Duration): State<Op.Meat.Marinate> {
    require(elapsed in 15.minutes..45.minutes) { "Marinate: duration must be in range 15..45 min" }

    return marinateState {
        this.opType = Op.Meat.Marinate::class
        // полагается, что простой маринад не меняет КБЖУ
        this.macronutrients = this@stateForMarinate.macronutrients
        // полагается, что в процессе маринования вес увеличивается на 7%
        this.quantity = this@stateForMarinate.quantity.addPercent(0.07)
        this.elapsed = this@stateForMarinate.elapsed + elapsed
        this.value = this@stateForMarinate.value + elapsed.simpleValueChain(0.75, 20.valueChain)
    }
}

/**
 * Бизнес-правила приемки компонентов гриля из хранилища и изменение состояния.
 *
 * @receiver исходный ресурс
 * @param elapsed затраченное операцией время
 * @return состояние [State] принятого для готовки гриля
 * */
public fun Resource.Grill.stateForGetGrill(elapsed: Duration): State<Op.Grill> {
    require(quantity in 500.weight..1000.weight) { "Grill acceptance: weight must be in range [500..1000g]" }
    require(elapsed in 0.5.minutes..2.minutes) { "Grill acceptance: duration mus be in range [0.5..2 min]" }

    return getGrillState {
        this.opType = Op.Grill.Get::class
        this.macronutrients = this@stateForGetGrill.macronutrients
        this.quantity = this@stateForGetGrill.quantity
        this.elapsed = elapsed
        this.value = elapsed.simpleValueChain(0.10, 10.valueChain)
    }
}

/**
 * Бизнес-правила подготовки гриля и изменение состояния.
 *
 * @receiver состояние принятых компонентов гриля
 * @param elapsed затраченное операцией время
 * @return состояние [State] готовности гриля
 * */
public fun State<Op.Grill.Get>.stateForCheckGrill(elapsed: Duration): State<Op.Grill.Check> {
    require(elapsed in 30.minutes..40.minutes) { "Prepare grill: duration must be in range [30..40 min]" }

    return checkGrillState {
        this.opType = Op.Grill.Check::class
        this.macronutrients = this@stateForCheckGrill.macronutrients
        this.quantity = this@stateForCheckGrill.quantity
        this.elapsed = this@stateForCheckGrill.elapsed + elapsed
    }
}
