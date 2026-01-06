package ru.it_arch.tools.samples.ribeye.storage

import ru.it_arch.tools.samples.ribeye.data.Expiration
import ru.it_arch.tools.samples.ribeye.data.Macronutrients
import ru.it_arch.tools.samples.ribeye.data.Quantity
import ru.it_arch.tools.samples.ribeye.data.Resource

/**
 * Контейнер хранилища для разных типов ресурсов — штучных, весовых.
 *
 * @param T тип хранимых данных
 * @param Q тип количества
 * */
internal sealed interface Slot<T : Resource, Q: Quantity> {

    /**
     *
     * @param requestQuantity
     * @return
     * */
    fun get(requestQuantity: Q): Result<T>

    /**
     * Поштучное хранение с общим сроком годности.
     * */
    class Piece<T: Resource>(
        private val macronutrients: Macronutrients,
        private val expiration: Expiration,
        private val element: T,
        capacity: Quantity.Piece
    ) : Slot<T, Quantity.Piece> {

        private var size = capacity

        override fun get(requestQuantity: Quantity.Piece): Result<T> =
            (size - requestQuantity).takeIf { it.isNotNegative() }
                ?.also { size = it }
                ?.let {
                    // Реально отпускаемое количество может не соответствовать запрашиваемому
                    val resultQuantity = requestQuantity
                    Result.success(element.fork(macronutrients, resultQuantity, expiration))
                } ?: emptySlot()
    }

    /** Весовое хранение с общим сроком годности. */
    class Weight<T: Resource>(
        private val macronutrients: Macronutrients,
        private val expiration: Expiration,
        private val element: T,
        capacity: Quantity.Weight
    ) : Slot<T, Quantity.Weight> {

        private var size = capacity

        override fun get(requestQuantity: Quantity.Weight): Result<T> =
            (size - requestQuantity).takeIf { it.isNotNegative() }
                ?.also { size = it }
                ?.let {
                    // Реально отпускаемое количество может не соответствовать запрашиваемому.
                    // Усушка, утруска, обвес и прочая складская магия :-)
                    val resultQuantity = requestQuantity
                    Result.success(element.fork(macronutrients, resultQuantity, expiration))
                } ?: emptySlot()
    }

    /** Поштучное хранение товара в упаковке со своим сроком годности и весом/кол-вом. */
    class Pack<T: Resource>(
        private val capacity: Quantity.Piece
    ) : Slot<T, Quantity.Piece> {

        private val slot = ArrayDeque<T>(capacity.boxed)

        override fun get(requestQuantity: Quantity.Piece): Result<T> =
            slot.removeFirstOrNull()?.let { Result.success(it) } ?: emptySlot()

        fun add(resource: T): Result<Unit> =
            resource.takeIf { slot.size < capacity.boxed }
                ?.also(slot::add)
                ?.let { Result.success(Unit) }
                ?: slotOverflow()
    }
}
