package ru.it_arch.tools.samples.ribeye.storage

import ru.it_arch.tools.samples.ribeye.Resource
import kotlin.reflect.KClass

internal fun <T : Resource> notFound(type: KClass<out T>): Result<T> =
    Result.failure(StorageError.NotFound(type.simpleName.orEmpty()))

internal fun <T : Resource> T.rotten(): Result<Unit> =
    Result.failure(
        StorageError.RottenResource(this::class.simpleName!!, expiration)
    )

