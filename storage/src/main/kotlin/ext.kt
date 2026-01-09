package ru.it_arch.tools.samples.ribeye.storage

import ru.it_arch.tools.samples.ribeye.data.Resource
import ru.it_arch.tools.samples.ribeye.storage.slot.Slot
import kotlin.reflect.KClass

internal fun <T : Resource> notFound(type: KClass<out T>): Result<T> =
    Result.failure(StorageError("Resource ${type.simpleName} not found"))

internal fun staleVersion(slotType: Slot.Pack, ) : StaleSlotVersion =
    StaleSlotVersion()
