package ru.it_arch.tools.samples.ribeye

import ru.it_arch.tools.samples.ribeye.data.Resource
import kotlin.reflect.KClass

public interface WriteResourceRepository : ResourceRepository {
    /**
     * Добавление ресурса [Resource]
     *
     * @param T тип ресурса [Resource]
     * @param type [KClass] типа ресурса [Resource]
     * @param resource добавляемый ресурс
     * @return [Result] в случае успеха [Unit] или ошибка
     * */
    public suspend fun <T : Resource> putByType(type: KClass<out T>, resource: T): Result<Unit>
}
