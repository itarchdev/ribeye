package ru.it_arch.tools.samples.ribeye

import ru.it_arch.tools.samples.ribeye.data.Quantity
import ru.it_arch.tools.samples.ribeye.data.Resource

public inline fun <reified T : Resource> ResourceRepository.get(requestQuantity: Quantity): Result<T> =
    getByType(T::class, requestQuantity)

public inline fun <reified T : Resource> ResourceRepository.put(resource: T): Result<Unit> =
    putByType(T::class, resource)
