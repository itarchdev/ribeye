package ru.it_arch.tools.samples.ribeye

import ru.it_arch.tools.samples.ribeye.data.Quantity
import ru.it_arch.tools.samples.ribeye.data.Resource
import kotlin.reflect.KClass

public interface ResourceRepository {
    /**
     * Получение ресурса [Resource] в требуемом количестве
     *
     * @param T тип ресурса [Resource]
     * @param type [KClass] типа ресурса [Resource]
     * @param requestQuantity требуемое количество
     * @return [Result] конкретного ресурса [Resource] или ошибка получения
     * */
    public fun <T : Resource> getByType(type: KClass<out T>, requestQuantity: Quantity): Result<T>

    /**
     * Добавление ресурса [Resource]
     *
     * @param T тип ресурса [Resource]
     * @param type [KClass] типа ресурса [Resource]
     * @param resource добавляемый ресурс
     * @return [Result] в случае успеха [Unit] или ошибка
     * */
    public fun <T : Resource> putByType(type: KClass<out T>, resource: T): Result<Unit>
}
