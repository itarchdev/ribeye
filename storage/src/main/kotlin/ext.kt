package ru.it_arch.tools.samples.ribeye.storage

public inline fun <reified T: ResourceOld> notFound(): Result<T> =
    Result.failure(StorageError("Resource ${T::class.simpleName} not found"))
