package ru.it_arch.tools.samples.ribeye

import ru.it_arch.tools.samples.ribeye.data.Quantity
import ru.it_arch.tools.samples.ribeye.data.Resource

public suspend inline fun <reified T : Resource> WriteResourceRepository.pull(requestQuantity: Quantity): Result<T> =
    pullByType(T::class, requestQuantity)

public suspend inline fun <reified T : Resource> WriteResourceRepository.put(resource: T): Result<Unit> =
    putByType(T::class, resource)

public suspend inline fun <reified T : Resource, reified Q: Quantity> WriteResourceRepository.size(): Q =
    sizeByType(T::class)
