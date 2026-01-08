package ru.it_arch.tools.samples.ribeye.storage.slot

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Instant

/**
 * Типонезависимый контейнер (слот) хранилища для разных видов ресурсов — упакованных, штучных, весовых.
 * Внутренее представление — строковое, по факту — хардкодинг в JSON-формате. Однако контейнер
 * не должен знать о фактическом формате представления его элементов. Сериализация/десериализация
 * осуществляется уровнем выше.
 * */
internal sealed interface Slot {

    /** текущая заполненность слота */
    suspend fun size(): Number

    /**
     *
     * @param requestQuantity
     * @return
     * */
    suspend fun get(requestQuantity: String): Result<String>

    /**
     * Поштучное хранение с общим сроком годности.
     * */
    class Piece(
        /** JSON */
        private val macronutrients: String,
        /** Instant */
        private val expiration: Instant,
        capacity: Int
    ) : Slot {

        private val mutex = Mutex()
        private var _size = capacity
        override suspend fun size(): Int =
            mutex.withLock { _size }

        override suspend fun get(requestQuantity: String): Result<String> =
            withContext(Dispatchers.Default) {
                mutex.withLock {
                    (_size - requestQuantity.toInt()).takeIf { it >= 0 }
                        ?.also { _size = it }
                        ?.let {
                            // Реально отпускаемое количество может не соответствовать запрашиваемому.
                            // Здесь можно подменить это значение и потом решать вопрос с ревизией
                            val resultQuantity = requestQuantity
                            Result.success(
                                buildResponse(
                                    macronutrients,
                                    resultQuantity,
                                    expiration
                                )
                            )
                        } ?: emptySlot()
                }
            }
    }

    /** Весовое хранение с общим сроком годности. */
    class Weight(
        /** JSON */
        private val macronutrients: String,
        /** Instant */
        private val expiration: Instant,
        capacity: Long
    ) : Slot {

        private val mutex = Mutex()
        private var _size = capacity
        override suspend fun size(): Long =
            mutex.withLock { _size }

        override suspend fun get(requestQuantity: String): Result<String> =
            withContext(Dispatchers.Default) {
                mutex.withLock {
                    (_size - requestQuantity.toLong()).takeIf { it >= 0 }
                        ?.also { _size = it }
                        ?.let {
                            // Реально отпускаемое количество может не соответствовать запрашиваемому.
                            // Усушка, утруска, обвес и прочая складская магия :-)
                            val resultQuantity = requestQuantity
                            Result.success(
                                buildResponse(
                                    macronutrients,
                                    resultQuantity,
                                    expiration
                                )
                            )
                        } ?: emptySlot()
                }
            }
    }

    /** Поштучное хранение ресурса в упаковке со своим сроком годности и весом/кол-вом. */
    class Pack(
        private val capacity: Int
    ) : Slot {

        private val mutex = Mutex()
        private val slot = ArrayDeque<String>(capacity)

        override suspend fun size(): Int =
            mutex.withLock { slot.size }

        /**
         * Получение
         * */
        override suspend fun get(requestQuantity: String): Result<String> =
            withContext(Dispatchers.Default) {
                mutex.withLock {
                    // Найти подходящий ресурс в слоте по критерию веса — большего или равным запрашиваемому
                    // и извлечь его из слота
                    requestQuantity.toIntOrNull()?.let { intQuantity ->
                        slot.firstOrNull { it quantityIsNotLessThan intQuantity }
                            ?.let { el -> Result.success(el).also { slot.remove(el) } }
                            ?: emptySlot()
                    } ?: error("request quantity must be Int")
                }
            }

        suspend fun add(resource: String): Result<Unit> =
            withContext(Dispatchers.Default) {
                mutex.withLock {
                    resource.takeIf { slot.size < capacity }
                        ?.also(slot::add)
                        ?.let { Result.success(Unit) }
                        ?: slotOverflow()
                }
            }

        /**
         * @receiver строковое представление ресурса
         * @param request запрашиваемое количество
         * @return результат поиска
         * */
        infix fun String.quantityIsNotLessThan(request: Int): Boolean =
            FIND_QUNTITY_RE.find(this)?.groupValues?.get(1)?.toIntOrNull()?.let { it >= request }
                ?: false

        companion object {
            val FIND_QUNTITY_RE = """"quantity"\s*:\s*(\d+)""".toRegex()
        }
    }

    companion object {
        const val MACRONUTRIENTS_PLACEHOLDER = "{macronutrients}"
        const val QUANTITY_PLACEHOLDER = "{quantity}"
        const val EXPIRATION_PLACEHOLDER = "{expiration}"

        const val RESOURCE_TEMPLATE = """{
    "macronutrients": $MACRONUTRIENTS_PLACEHOLDER,
    "quantity": $QUANTITY_PLACEHOLDER,
    "expiration": "$EXPIRATION_PLACEHOLDER"
}"""

        /**
         *  Генерация элемента хранения в соответствии с параметрами слота, где он хранится.
         *  Полагается, что слоты хранят элементы в виде строки (JSON).
         *  Реальное хранение заменяется генерацией элемента .
         */
        fun buildResponse(macronutrients: String, quantity: String, expiration: Instant): String =
            RESOURCE_TEMPLATE.replace(MACRONUTRIENTS_PLACEHOLDER, macronutrients)
                .replace(QUANTITY_PLACEHOLDER, quantity)
                .replace(EXPIRATION_PLACEHOLDER, expiration.toString())
    }
}
