package ru.it_arch.tools.samples.ribeye.dsl.impl

import kotlinx.serialization.json.Json
import ru.it_arch.tools.samples.ribeye.dsl.CookingProcess
import ru.it_arch.tools.samples.ribeye.dsl.Expiration
import ru.it_arch.tools.samples.ribeye.dsl.Macronutrients
import ru.it_arch.tools.samples.ribeye.dsl.Op
import ru.it_arch.tools.samples.ribeye.dsl.Quantity
import ru.it_arch.tools.samples.ribeye.dsl.Resource
import ru.it_arch.tools.samples.ribeye.dsl.State
import ru.it_arch.tools.samples.ribeye.dsl.ValueChain
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.time.Duration
import kotlin.time.Instant

internal val json = Json {
    prettyPrint = true
}

public inline fun cookingProcess(block: CookingProcessImpl.Builder.() -> Unit): CookingProcess =
    CookingProcessImpl.Builder().apply(block).build()

public inline fun macronutrients(block: MacronutrientsImpl.DslBuilder.() -> Unit): Macronutrients =
    MacronutrientsImpl.DslBuilder().apply(block).build()

public fun Macronutrients.toBuilder(): MacronutrientsImpl.Builder =
    MacronutrientsImpl.Builder().apply {
        proteins = this@toBuilder.proteins
        fats = this@toBuilder.fats
        carbs = this@toBuilder.carbs
        calories = this@toBuilder.calories
    }

public fun Macronutrients.toDslBuilder(): MacronutrientsImpl.DslBuilder =
    MacronutrientsImpl.DslBuilder().apply {
        proteins = this@toDslBuilder.proteins.boxed
        fats = this@toDslBuilder.fats.boxed
        carbs = this@toDslBuilder.carbs.boxed
        calories = this@toDslBuilder.calories.boxed
    }

public fun Macronutrients.format(): String =
    json.encodeToString(this as MacronutrientsImpl)

public fun String.toMacronutrients(): Macronutrients =
    json.decodeFromString<MacronutrientsImpl>(this)

public val Int.valueChain: ValueChain
    get() = ValueChainImpl(BigDecimal.valueOf(toLong()).setScale(ValueChain.SCALE, RoundingMode.HALF_UP))

public val Double.valueChain: ValueChain
    get() = ValueChainImpl(BigDecimal.valueOf(this).setScale(ValueChain.SCALE, RoundingMode.HALF_UP))

public val Instant.expiration: Expiration
    get() = ExpirationImpl(this)

// Quantity

public val Long.weight: Quantity.Weight
    get() = QuantityWeightImpl(this)

public val Int.weight: Quantity.Weight
    get() = QuantityWeightImpl(toLong())

public val Int.piece: Quantity.Piece
    get() = QuantityPieceImpl(this)

// Macronutrients

public val Double.proteins: Macronutrients.Proteins
    get() = MacronutrientsImpl.ProteinsImpl(this)

public val Double.fats: Macronutrients.Fats
    get() = MacronutrientsImpl.FatsImpl(this)

public val Double.carbohydrates: Macronutrients.Carbohydrates
    get() = MacronutrientsImpl.CarbohydratesImpl(this)

public val Double.calories: Macronutrients.Kcal
    get() = MacronutrientsImpl.KcalImpl(this)

// Meat

public inline fun meat(block: MeatImpl.DslBuilder.() -> Unit): Resource.Meat =
    MeatImpl.DslBuilder().apply(block).build()

public fun Resource.Meat.toBuilder(): MeatImpl.Builder =
    MeatImpl.Builder().apply {
        macronutrients = this@toBuilder.macronutrients
        quantity = this@toBuilder.quantity
        expiration = this@toBuilder.expiration
    }

public fun Resource.Meat.toDslBuilder(): MeatImpl.DslBuilder =
    MeatImpl.DslBuilder().apply {
        macronutrients = this@toDslBuilder.macronutrients
        quantity = this@toDslBuilder.quantity.boxed
        expiration = this@toDslBuilder.expiration.boxed
    }

public fun Resource.Meat.format(): String =
    json.encodeToString(this as MeatImpl)

public fun String.toMeat(): Resource.Meat =
    json.decodeFromString<MeatImpl>(this)

// Grill

public inline fun grill(block: GrillImpl.DslBuilder.() -> Unit): Resource.Grill =
    GrillImpl.DslBuilder().apply(block).build()

public fun Resource.Grill.toBuilder(): GrillImpl.Builder =
    GrillImpl.Builder().apply {
        macronutrients = this@toBuilder.macronutrients
        quantity = this@toBuilder.quantity
        expiration = this@toBuilder.expiration
    }

public fun Resource.Grill.toDslBuilder(): GrillImpl.DslBuilder =
    GrillImpl.DslBuilder().apply {
        macronutrients = this@toDslBuilder.macronutrients
        quantity = this@toDslBuilder.quantity.boxed
        expiration = this@toDslBuilder.expiration.boxed
    }

public fun Resource.Grill.format(): String =
    json.encodeToString(this as GrillImpl)

public fun String.toGrill(): Resource.Grill =
    json.decodeFromString<GrillImpl>(this)

// Sauce

public inline fun sauceIngredients(
    block: SauceIngredientsImpl.DslBuilder.() -> Unit
): Resource.SauceIngredients =
    SauceIngredientsImpl.DslBuilder().apply(block).build()

public fun Resource.SauceIngredients.toBuilder(): SauceIngredientsImpl.Builder =
    SauceIngredientsImpl.Builder().apply {
        macronutrients = this@toBuilder.macronutrients
        quantity = this@toBuilder.quantity
        expiration = this@toBuilder.expiration
    }

public fun Resource.SauceIngredients.toDslBuilder(): SauceIngredientsImpl.DslBuilder =
    SauceIngredientsImpl.DslBuilder().apply {
        macronutrients = this@toDslBuilder.macronutrients
        quantity = this@toDslBuilder.quantity.boxed
        expiration = this@toDslBuilder.expiration.boxed
    }

public fun Resource.SauceIngredients.format(): String =
    json.encodeToString(this as SauceIngredientsImpl)

public fun String.toSauceIngredients(): Resource.SauceIngredients =
    json.decodeFromString<SauceIngredientsImpl>(this)

// Rosemary

public inline fun rosemary(block: RosemaryImpl.DslBuilder.() -> Unit): Resource.Rosemary =
    RosemaryImpl.DslBuilder().apply(block).build()

public fun Resource.Rosemary.toBuilder(): RosemaryImpl.Builder =
    RosemaryImpl.Builder().apply {
        macronutrients = this@toBuilder.macronutrients
        quantity = this@toBuilder.quantity
        expiration = this@toBuilder.expiration
    }

public fun Resource.Rosemary.toDslBuilder(): RosemaryImpl.DslBuilder =
    RosemaryImpl.DslBuilder().apply {
        macronutrients = this@toDslBuilder.macronutrients
        quantity = this@toDslBuilder.quantity.boxed
        expiration = this@toDslBuilder.expiration.boxed
    }

public fun Resource.Rosemary.format(): String =
    json.encodeToString(this as RosemaryImpl)

public fun String.toRosemary(): Resource.Rosemary =
    json.decodeFromString<RosemaryImpl>(this)

// State

public fun getMeatState(block: StateImpl.Builder<Op.Meat.Get>.() -> Unit): State<Op.Meat.Get> =
    StateImpl.Builder<Op.Meat.Get>().apply(block).build()

public fun checkMeatState(block: StateImpl.Builder<Op.Meat.Check>.() -> Unit): State<Op.Meat.Check> =
    StateImpl.Builder<Op.Meat.Check>().apply(block).build()

public fun marinateState(block: StateImpl.Builder<Op.Meat.Marinate>.() -> Unit): State<Op.Meat.Marinate> =
    StateImpl.Builder<Op.Meat.Marinate>().apply(block).build()

public fun getGrillState(block: StateImpl.Builder<Op.Grill.Get>.() -> Unit): State<Op.Grill.Get> =
    StateImpl.Builder<Op.Grill.Get>().apply(block).build()

public fun checkGrillState(block: StateImpl.Builder<Op.Grill.Check>.() -> Unit): State<Op.Grill.Check> =
    StateImpl.Builder<Op.Grill.Check>().apply(block).build()

public fun getSauceIngredientsState(block: StateImpl.Builder<Op.Sauce.Get>.() -> Unit): State<Op.Sauce.Get> =
    StateImpl.Builder<Op.Sauce.Get>().apply(block).build()

public fun prepareSauceState(block: StateImpl.Builder<Op.Sauce.Prepare>.() -> Unit): State<Op.Sauce.Prepare> =
    StateImpl.Builder<Op.Sauce.Prepare>().apply(block).build()

public fun getRosemaryState(block: StateImpl.Builder<Op.Rosemary.Get>.() -> Unit): State<Op.Rosemary.Get> =
    StateImpl.Builder<Op.Rosemary.Get>().apply(block).build()

public fun roastRosemaryState(block: StateImpl.Builder<Op.Rosemary.Roast>.() -> Unit): State<Op.Rosemary.Roast> =
    StateImpl.Builder<Op.Rosemary.Roast>().apply(block).build()

public fun steakStartState(block: StateImpl.Builder<Op.Meat.PrepareForRoasting>.() -> Unit): State<Op.Meat.PrepareForRoasting> =
    StateImpl.Builder<Op.Meat.PrepareForRoasting>().apply(block).build()

public fun roastMeatState(block: StateImpl.Builder<Op.Meat.Roast>.() -> Unit): State<Op.Meat.Roast> =
    StateImpl.Builder<Op.Meat.Roast>().apply(block).build()

public fun serveState(block: StateImpl.Builder<Op.Meat.Serve>.() -> Unit): State<Op.Meat.Serve> =
    StateImpl.Builder<Op.Meat.Serve>().apply(block).build()

public fun finishState(block: StateImpl.Builder<Op.Finish>.() -> Unit): State<Op.Finish> =
    StateImpl.Builder<Op.Finish>().apply(block).build()


