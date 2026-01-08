package ru.it_arch.tools.samples.ribeye.storage

import ru.it_arch.tools.samples.ribeye.data.Resource
import kotlin.reflect.KClass

public fun <T : Resource> notFound(type: KClass<out T>): Result<T> =
    Result.failure(StorageError("Resource ${type.simpleName} not found"))

internal fun emptySlot(): Result<String> =
    Result.failure((StorageError("Slot is empty")))

internal fun slotOverflow(): Result<Unit> =
    Result.failure((StorageError("Slot is full")))
