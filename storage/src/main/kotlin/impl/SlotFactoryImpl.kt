package ru.it_arch.tools.samples.ribeye.storage.impl

import ru.it_arch.tools.samples.ribeye.data.Expiration
import ru.it_arch.tools.samples.ribeye.data.Macronutrients
import ru.it_arch.tools.samples.ribeye.data.Quantity
import ru.it_arch.tools.samples.ribeye.storage.SlotFactory
import ru.it_arch.tools.samples.ribeye.storage.slot.Slot

internal class SlotFactoryImpl(
    private val meatCapacity: Quantity.Piece,
    private val grillCapacity: Quantity.Weight,
    private val sauceCapacity: Quantity.Weight,
    private val rosemaryCapacity: Quantity.Piece
) : SlotFactory {

    override fun slotForMeat(): Slot.Reusable =
        Slot.Pack(meatCapacity.boxed)

    override fun slotForGrill(
        macronutrients: Macronutrients,
        expiration: Expiration
    ): Slot.Disposable =
        Slot.Weight(macronutrients.format(), expiration.boxed, grillCapacity.boxed)

    override fun slotForSauce(
        macronutrients: Macronutrients,
        expiration: Expiration
    ): Slot.Disposable =
        Slot.Weight(macronutrients.format(), expiration.boxed, sauceCapacity.boxed)

    override fun slotForRosemary(
        macronutrients: Macronutrients,
        expiration: Expiration
    ): Slot.Disposable =
        Slot.Piece(macronutrients.format(), expiration.boxed, rosemaryCapacity.boxed)
}
