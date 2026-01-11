package ru.it_arch.tools.samples.ribeye.storage

import ru.it_arch.tools.samples.ribeye.data.Resource
import kotlin.reflect.KClass

internal fun <T : Resource> notFound(type: KClass<out T>): Result<T> =
    Result.failure(StorageError.NotFound(type.simpleName.orEmpty()))
