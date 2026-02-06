package ru.it_arch.tools.samples.ribeye.storage

import ru.it_arch.tools.samples.ribeye.Expiration

public sealed class StorageError(msg: String) : RuntimeException(msg) {
    public class NotFound(
        resourceName: String
    ) : StorageError("Resource $resourceName not found")

    public class ExhaustedRetries(
        slotName: String
    ) : StorageError("Exhausted retries for slot $slotName")

    public class RottenResource(
        resourceName: String,
        expiration: Expiration
    ) : StorageError("$resourceName has expired. Storage expiration: ${expiration.localFormat}")
}
