package ru.it_arch.tools.samples.ribeye.storage

import kotlin.reflect.KClass

/**
 * Репозиторий
 * */
public interface Storage {
    public fun <T : ResourceOld> get(type: KClass<T>, quantity: Quantity): Result<T>
    //public fun <T : Resource> add(type: KClass<T>, resource: T, quantity: Quantity): Result<Unit>
}
