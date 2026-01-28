package ru.it_arch.tools.samples.ribeye.storage.impl

import kotlinx.serialization.json.Json
import ru.it_arch.tools.samples.ribeye.dsl.Macronutrients
import ru.it_arch.tools.samples.ribeye.dsl.Quantity
import ru.it_arch.tools.samples.ribeye.dsl.Resource
import ru.it_arch.tools.samples.ribeye.dsl.impl.GrillImpl
import ru.it_arch.tools.samples.ribeye.dsl.impl.MacronutrientsImpl
import ru.it_arch.tools.samples.ribeye.dsl.impl.MeatImpl
import ru.it_arch.tools.samples.ribeye.dsl.impl.QuantityPieceImpl
import ru.it_arch.tools.samples.ribeye.dsl.impl.QuantityWeightImpl
import ru.it_arch.tools.samples.ribeye.dsl.impl.RosemaryImpl
import ru.it_arch.tools.samples.ribeye.dsl.impl.SauceIngredientsImpl
import ru.it_arch.tools.samples.ribeye.storage.SlotFactory

internal val json = Json {
    prettyPrint = true
}

// Slot factory

public fun slotFactory(
    meatCapacity: Quantity.Piece,
    grillCapacity: Quantity.Weight,
    sauceCapacity: Quantity.Weight,
    rosemaryCapacity: Quantity.Piece
): SlotFactory = SlotFactoryImpl(meatCapacity, grillCapacity, sauceCapacity, rosemaryCapacity)
