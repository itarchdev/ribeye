package ru.it_arch.tools.samples.ribeye.storage.impl

import ru.it_arch.tools.samples.ribeye.data.Macronutrients
import ru.it_arch.tools.samples.ribeye.data.Resource

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

public fun Macronutrients.toJson(): String = TODO()

public fun String.macronutrientsFromJson(): Macronutrients = TODO()

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

public fun Resource.Meat.toJson(): String =
    TODO()

public fun String.meatFromJson(): Resource.Meat = TODO()

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

public fun Resource.Grill.toJson(): String = TODO()

public fun String.grillFromJson(): Resource.Grill = TODO()

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

public fun Resource.SauceIngredients.toJson(): String = TODO()

public fun String.sauceFromJson(): Resource.SauceIngredients = TODO()

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

public fun Resource.Rosemary.toJson(): String = TODO()

public fun String.rosemaryFromJson(): Resource.Rosemary = TODO()
