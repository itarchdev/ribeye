package ru.it_arch.tools.samples.ribeye.storage.slot

import ru.it_arch.tools.samples.ribeye.storage.StorageError

internal fun emptySlot(): Result<String> =
    Result.failure((StorageError("Slot is empty")))

internal fun slotOverflow(): Result<Int> =
    Result.failure((StorageError("Slot is full")))
