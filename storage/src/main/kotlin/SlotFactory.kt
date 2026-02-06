package ru.it_arch.tools.samples.ribeye.storage

import ru.it_arch.tools.samples.ribeye.Expiration
import ru.it_arch.tools.samples.ribeye.Macronutrients
import ru.it_arch.tools.samples.ribeye.storage.slot.Slot

/**
 * Фабрика создания слотов под различного рода ресурсов.
 * */
public interface SlotFactory {
    public fun slotForMeat(): Slot.Reusable

    public fun slotForGrill(macronutrients: Macronutrients, expiration: Expiration): Slot.Disposable

    public fun slotForSauce(macronutrients: Macronutrients, expiration: Expiration): Slot.Disposable

    public fun slotForRosemary(macronutrients: Macronutrients, expiration: Expiration): Slot.Disposable
}
