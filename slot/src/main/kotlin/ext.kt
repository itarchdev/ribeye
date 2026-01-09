package ru.it_arch.tools.samples.ribeye.storage.slot

internal fun emptySlot(): Result<String> =
    Result.failure(SlotError("Slot is empty"))

internal fun slotOverflow(): Result<Int> =
    Result.failure(SlotError("Slot is full"))
