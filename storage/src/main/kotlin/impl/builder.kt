package ru.it_arch.tools.samples.ribeye.storage.impl

import kotlinx.serialization.json.Json
import ru.it_arch.tools.samples.ribeye.Quantity
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
