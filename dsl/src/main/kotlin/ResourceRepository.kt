package ru.it_arch.tools.samples.ribeye

import kotlin.reflect.KClass

public interface ResourceRepository {
    /**
     *
     * */
    public suspend fun <T : Resource, Q : Quantity> sizeByType(type: KClass<out T>): Q

    /**
     * Извлечение ресурса [Resource] в требуемом количестве
     *
     * @param T тип ресурса [Resource]
     * @param type [KClass] типа ресурса [Resource]
     * @param requestQuantity требуемое количество
     * @return [Result] конкретного ресурса [Resource] или ошибка получения
     * */
    public suspend fun <T : Resource, Q : Quantity> pullByType(type: KClass<out T>, requestQuantity: Q): Result<T>

}
