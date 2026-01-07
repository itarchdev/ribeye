package ru.it_arch.tools.samples.ribeye.storage

/**
 * Типонезависимый контейнер хранилища для разных видов ресурсов — штучных, весовых.
 * Внутренее представление — строковое, по факту — JSON, но контейнер не должен знать о фактическом
 * формате представления его элементов.
 * */
internal sealed interface Slot {

    /**
     *
     * @param requestQuantity
     * @return
     * */
    fun get(requestQuantity: String): Result<String>

    /**
     * Поштучное хранение с общим сроком годности.
     * */
    class Piece(
        /** JSON */
        private val macronutrients: String,
        /** Instant */
        private val expiration: String,
        capacity: Int
    ) : Slot {

        private var size = capacity

        override fun get(requestQuantity: String): Result<String> =
            (size - requestQuantity.toInt()).takeIf { it >= 0 }
                ?.also { size = it }
                ?.let {
                    // Реально отпускаемое количество может не соответствовать запрашиваемому.
                    // Здесь можно подменить это значение и потом решать вопрос с ревизией
                    val resultQuantity = requestQuantity
                    Result.success(buildJson(macronutrients, resultQuantity, expiration))
                } ?: emptySlot()
    }

    /** Весовое хранение с общим сроком годности. */
    class Weight(
        /** JSON */
        private val macronutrients: String,
        /** Instant */
        private val expiration: String,
        capacity: Long
    ) : Slot {

        private var size = capacity

        override fun get(requestQuantity: String): Result<String> =
            (size - requestQuantity.toLong()).takeIf { it >= 0 }
                ?.also { size = it }
                ?.let {
                    // Реально отпускаемое количество может не соответствовать запрашиваемому.
                    // Усушка, утруска, обвес и прочая складская магия :-)
                    val resultQuantity = requestQuantity
                    Result.success(buildJson(macronutrients, resultQuantity, expiration))
                } ?: emptySlot()
    }

    /** Поштучное хранение ресурса в упаковке со своим сроком годности и весом/кол-вом. */
    class Pack(
        private val capacity: Int
    ) : Slot {

        private val slot = ArrayDeque<String>(capacity)

        /**
         * Получение
         * */
        override fun get(requestQuantity: String): Result<String> =
            // Найти подходящий ресурс в слоте по критерию веса — большего или равным запрашиваемому
            // и извлечь его из слота
            requestQuantity.toIntOrNull()?.let { intQuantity ->
                slot.firstOrNull { it quantityIsNotLessThan intQuantity }
                    ?.let { el -> Result.success(el).also { slot.remove(el) } }
                    ?: emptySlot()
            } ?: error("request quantity must be Int")

        fun add(resource: String): Result<Unit> =
            resource.takeIf { slot.size < capacity }
                ?.also(slot::add)
                ?.let { Result.success(Unit) }
                ?: slotOverflow()

        /**
         * @receiver строковое представление ресурса
         * @param request запрашиваемое количество
         * @return результат поиска
         * */
        private infix fun String.quantityIsNotLessThan(request: Int): Boolean =
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

        fun buildJson(macronutrients: String, quantity: String, expiration: String): String =
            RESOURCE_TEMPLATE.replace(MACRONUTRIENTS_PLACEHOLDER, macronutrients)
                .replace(QUANTITY_PLACEHOLDER, quantity)
                .replace(EXPIRATION_PLACEHOLDER, expiration)
    }
}
