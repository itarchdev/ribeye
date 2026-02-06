package ru.it_arch.tools.samples.ribeye

import kotlinx.serialization.json.Json
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.time.Instant

internal val json = Json {
    prettyPrint = true
}

public inline fun cookingProcess(block: CookingProcessImpl.Builder.() -> Unit): CookingProcess =
    CookingProcessImpl.Builder().apply(block).build()

public inline fun macronutrients(block: Macronutrients.DslBuilder.() -> Unit): Macronutrients =
    Macronutrients.DslBuilder().apply(block).build()

public fun Macronutrients.toBuilder(): Macronutrients.Builder =
    Macronutrients.Builder().apply {
        proteins = this@toBuilder.proteins
        fats = this@toBuilder.fats
        carbs = this@toBuilder.carbs
        calories = this@toBuilder.calories
    }

public fun Macronutrients.toDslBuilder(): Macronutrients.DslBuilder =
    Macronutrients.DslBuilder().apply {
        proteins = this@toDslBuilder.proteins.boxed
        fats = this@toDslBuilder.fats.boxed
        carbs = this@toDslBuilder.carbs.boxed
        calories = this@toDslBuilder.calories.boxed
    }

/*
public fun Macronutrients.format(): String =
    json.encodeToString(this as MacronutrientsImpl)

public fun String.toMacronutrients(): Macronutrients =
    json.decodeFromString<MacronutrientsImpl>(this)
 */

public val Int.valueChain: ValueChain
    get() = ValueChain(BigDecimal.valueOf(toLong()).setScale(ValueChain.SCALE, RoundingMode.HALF_UP))

public val Double.valueChain: ValueChain
    get() = ValueChain(BigDecimal.valueOf(this).setScale(ValueChain.SCALE, RoundingMode.HALF_UP))

public val BigDecimal.valueChain: ValueChain
    get() = ValueChain(this)

public val Instant.expiration: Expiration
    get() = Expiration(this)

// Quantity

public val Long.weight: Quantity.Weight
    get() = Quantity.Weight(this)

public val Int.weight: Quantity.Weight
    get() = Quantity.Weight(toLong())

public val Int.piece: Quantity.Piece
    get() = Quantity.Piece(this)

// Macronutrients

public val Double.proteins: Macronutrients.Proteins
    get() = Macronutrients.Proteins(this)

public val Double.fats: Macronutrients.Fats
    get() = Macronutrients.Fats(this)

public val Double.carbohydrates: Macronutrients.Carbohydrates
    get() = Macronutrients.Carbohydrates(this)

public val Double.calories: Macronutrients.Kcal
    get() = Macronutrients.Kcal(this)


// State

public fun <T : Op> State<T>.toBuilder(): State.Builder<T> =
    State.Builder<T>().apply {
        this.opType = this@toBuilder.opType
        this.macronutrients = this@toBuilder.macronutrients
        this.quantity = this@toBuilder.quantity
        this.elapsed = this@toBuilder.elapsed
        this.value = this@toBuilder.value
    }

public fun getMeatState(block: State.Builder<Op.Meat.Get>.() -> Unit): State<Op.Meat.Get> =
    State.Builder<Op.Meat.Get>().apply(block).build()

public fun checkMeatState(block: State.Builder<Op.Meat.Check>.() -> Unit): State<Op.Meat.Check> =
    State.Builder<Op.Meat.Check>().apply(block).build()

public fun marinateState(block: State.Builder<Op.Meat.Marinate>.() -> Unit): State<Op.Meat.Marinate> =
    State.Builder<Op.Meat.Marinate>().apply(block).build()

public fun getGrillState(block: State.Builder<Op.Grill.Get>.() -> Unit): State<Op.Grill.Get> =
    State.Builder<Op.Grill.Get>().apply(block).build()

public fun checkGrillState(block: State.Builder<Op.Grill.Check>.() -> Unit): State<Op.Grill.Check> =
    State.Builder<Op.Grill.Check>().apply(block).build()

public fun getSauceIngredientsState(block: State.Builder<Op.Sauce.Get>.() -> Unit): State<Op.Sauce.Get> =
    State.Builder<Op.Sauce.Get>().apply(block).build()

public fun prepareSauceState(block: State.Builder<Op.Sauce.Prepare>.() -> Unit): State<Op.Sauce.Prepare> =
    State.Builder<Op.Sauce.Prepare>().apply(block).build()

public fun getRosemaryState(block: State.Builder<Op.Rosemary.Get>.() -> Unit): State<Op.Rosemary.Get> =
    State.Builder<Op.Rosemary.Get>().apply(block).build()

public fun roastRosemaryState(block: State.Builder<Op.Rosemary.Roast>.() -> Unit): State<Op.Rosemary.Roast> =
    State.Builder<Op.Rosemary.Roast>().apply(block).build()

public fun steakStartState(block: State.Builder<Op.Meat.PrepareForRoasting>.() -> Unit): State<Op.Meat.PrepareForRoasting> =
    State.Builder<Op.Meat.PrepareForRoasting>().apply(block).build()

public fun roastMeatState(block: State.Builder<Op.Meat.Roast>.() -> Unit): State<Op.Meat.Roast> =
    State.Builder<Op.Meat.Roast>().apply(block).build()

public fun serveState(block: State.Builder<Op.Meat.Serve>.() -> Unit): State<Op.Meat.Serve> =
    State.Builder<Op.Meat.Serve>().apply(block).build()

public fun finishState(block: State.Builder<Op.Finish>.() -> Unit): State<Op.Finish> =
    State.Builder<Op.Finish>().apply(block).build()
