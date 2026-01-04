package ru.it_arch.tools.samples.ribeye.storage.impl

import ru.it_arch.tools.samples.ribeye.storage.Macronutrients
import ru.it_arch.tools.samples.ribeye.storage.ResourceOld

public inline fun macronutrients(block: MacronutrientsImpl.Builder.() -> Unit): Macronutrients =
    MacronutrientsImpl.Builder().apply(block).build()

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

public inline fun meat(block: MeatImpl.DslBuilder.() -> Unit): ResourceOld.Meat =
    MeatImpl.DslBuilder().apply(block).build()

public fun ResourceOld.Meat.toBuilder(): MeatImpl.Builder =
    MeatImpl.Builder().apply { 
        macronutrients = this@toBuilder.macronutrients
        expiration = this@toBuilder.expiration
    }

public fun ResourceOld.Meat.toDslBuilder(): MeatImpl.DslBuilder =
    MeatImpl.DslBuilder().apply {
        macronutrients = this@toDslBuilder.macronutrients
        expiration = this@toDslBuilder.expiration.boxed
    }

public inline fun sauceIngredients(
    block: SauceIngredientsImpl.DslBuilder.() -> Unit
): ResourceOld.SauceIngredients =
    SauceIngredientsImpl.DslBuilder().apply(block).build()

public fun ResourceOld.SauceIngredients.toBuilder(): SauceIngredientsImpl.Builder =
    SauceIngredientsImpl.Builder().apply { 
        macronutrients = this@toBuilder.macronutrients
        expiration = this@toBuilder.expiration
    }

public fun ResourceOld.SauceIngredients.toDslBuilder(): SauceIngredientsImpl.DslBuilder =
    SauceIngredientsImpl.DslBuilder().apply {
        macronutrients = this@toDslBuilder.macronutrients
        expiration = this@toDslBuilder.expiration.boxed
    }

public inline fun rosematy(block: RosemaryImpl.DslBuilder.() -> Unit): ResourceOld.Rosemary =
    RosemaryImpl.DslBuilder().apply(block).build()

public fun ResourceOld.Rosemary.toBuilder(): RosemaryImpl.Builder =
    RosemaryImpl.Builder().apply { 
        macronutrients = this@toBuilder.macronutrients
        expiration = this@toBuilder.expiration
    }

public fun ResourceOld.Rosemary.toDslBuilder(): RosemaryImpl.DslBuilder =
    RosemaryImpl.DslBuilder().apply {
        macronutrients = this@toDslBuilder.macronutrients
        expiration = this@toDslBuilder.expiration.boxed
    }
