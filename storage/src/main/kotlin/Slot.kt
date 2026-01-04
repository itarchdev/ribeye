package ru.it_arch.tools.samples.ribeye.storage

/**
 * Контейнер хранилища для разных типов ресурсов — штучных, весовых.
 * */
public sealed interface Slot<T : ResourceOld> {

    public fun get(quantity: Quantity): Result<T>

    public interface ByUnit<T : ResourceOld> : Slot<T> {
        public val expiration: ResourceOld.Expiration
    }

    public interface ByPack<T : ResourceOld> : Slot<T>

    public interface ByWeight<T : ResourceOld> : Slot<T> {
        public val expiration: ResourceOld.Expiration
    }
}

public class ByUnitImpl<T : ResourceOld>(
    override val expiration: ResourceOld.Expiration,
    private val capacity: Int
) : Slot.ByUnit<T> {

    private val slot = ArrayDeque<T>(capacity)

    override fun get(quantity: Quantity): Result<T> =
        slot.removeFirstOrNull()?.let { Result.success(it) }
            ?: Result.failure(StorageError("Slot is empty"))
}
