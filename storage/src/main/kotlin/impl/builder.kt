package ru.it_arch.tools.samples.ribeye.storage.impl

import kotlinx.serialization.json.Json
import ru.it_arch.tools.samples.ribeye.data.Macronutrients
import ru.it_arch.tools.samples.ribeye.data.Quantity
import ru.it_arch.tools.samples.ribeye.data.Resource
import ru.it_arch.tools.samples.ribeye.storage.SlotFactory

internal val json = Json {
    prettyPrint = true
    /*
    serializersModule = SerializersModule {
        polymorphic(Macronutrients::class) {
            subclass(MacronutrientsImpl::class)
        }
        polymorphic(Resource.Meat::class) {
            subclass(MeatImpl::class)
        }
        polymorphic(Resource.Grill::class) {
            subclass(GrillImpl::class)
        }
        polymorphic(Resource.SauceIngredients::class) {
            subclass(SauceIngredientsImpl::class)
        }
        polymorphic(Resource.Rosemary::class) {
            subclass(RosemaryImpl::class)
        }
    }*/
}

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

// Quantity

public fun Long.toQuantity(): Quantity.Weight =
    QuantityWeightImpl(this)

public fun Int.toQuantity(): Quantity.Piece =
    QuantityPieceImpl(this)

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

// Slot factory

public fun slotFactory(
    meatCapacity: Quantity.Piece,
    grillCapacity: Quantity.Weight,
    sauceCapacity: Quantity.Weight,
    rosemaryCapacity: Quantity.Piece
): SlotFactory = SlotFactoryImpl(meatCapacity, grillCapacity, sauceCapacity, rosemaryCapacity)
