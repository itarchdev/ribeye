package ru.it_arch.tools.samples.ribeye.storage.impl

import ru.it_arch.tools.samples.ribeye.storage.Quantity
import ru.it_arch.tools.samples.ribeye.storage.ResourceOld
import ru.it_arch.tools.samples.ribeye.storage.Storage
import ru.it_arch.tools.samples.ribeye.storage.notFound
import kotlin.reflect.KClass

public class StorageImpl private constructor() : Storage {

    private val resources = mutableMapOf<KClass<out ResourceOld>, Slot<ResourceOld>>().apply {
        ResourceOld.types.forEach { this[it] = Slot() }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ResourceOld> get(type: KClass<T>, quantity: Quantity): Result<T> =
        (resources[type]?.get()?.let { Result.success(it) }
            ?: notFound()) as Result<T>

    override fun <T : ResourceOld> add(type: KClass<T>, resource: T, quantity: Quantity): Result<Unit> =
        runCatching {
            resources[type]!!.put(resource).let { Result.success(Unit) }
        }

    private class Slot<T : ResourceOld>(private val maxSize: Int = 10) {
        private val slot = ArrayDeque<T>()

        fun get(): T? =
            slot.removeFirstOrNull()

        fun put(resource: T) {
            require(slot.size < maxSize)
            slot.addLast(resource)
        }
    }

    public companion object {
        public operator fun invoke(): Storage =
            StorageImpl()
    }
}
