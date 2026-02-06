package ru.it_arch.tools.samples.ribeye

public suspend inline fun <reified T : Resource> ResourceRepository.pull(requestQuantity: Quantity): Result<T> =
    pullByType(T::class, requestQuantity)

public suspend inline fun <reified T : Resource> WriteResourceRepository.put(resource: T): Result<Unit> =
    putByType(T::class, resource)

public suspend inline fun <reified T : Resource, reified Q: Quantity> ResourceRepository.size(): Q =
    sizeByType(T::class)
