package ru.it_arch.tools.samples.ribeye.storage.slot

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.time.Instant

/**
 * Форматонезависимый (условно) слот хранения для разных видов ресурсов — упакованных, штучных, весовых.
 * Внутренее представление — строковое, по факту — хардкодинг в JSON-формате. Однако слот
 * не должен знать о фактическом формате представления его элементов. Сериализация/десериализация
 * должна осуществляться уровнем выше.
 * */
public sealed interface Slot {

    /** Вынесен с целью замены для Unit-тестов. По умолчанию — [Dispatchers.Default] */
    public val dispatcher: CoroutineDispatcher

    /** Текущая заполненность слота */
    public suspend fun size(): Number

    /**
     * Одноразовый слот, заменямый вместе с новым содержимым. Состояние ЖЦ определяется флагом [isActive]
     *
     * @param macronutrients строковое представление (JSON) КБЖУ
     * @param expiration строковое представление срока годности ([Instant])
     * @param capacity максимальный размер слота
     * */
    public sealed class Disposable(
        protected val macronutrients: String,
        protected val expiration: Instant,
        protected val capacity: Number,
    ) : Slot {

        /** Состояние слота. Необходим для предотвращения доступа к убитому слоту при конкурентном обретении слота */
        @Volatile
        protected var isActive: Boolean = true
        protected val mutex: Mutex = Mutex()

        /** Перевод слота в неактивное состояние */
        public fun kill() {
            isActive = false
        }

        /**
         * Извлечение ресурса из слота
         *
         * @param requestQuantity запрашиваемое количество
         * @return [Result] ресурса
         * */
        public abstract suspend fun pull(requestQuantity: Number): Result<String>
    }

    /**
     * Переиспользуемый слот. Состояние ЖЦ определяется сквозной версией [currentVersion]
     * для обеспечения [оптимистической блокировки](https://en.wikipedia.org/wiki/Optimistic_concurrency_control)
     * */
    @OptIn(ExperimentalAtomicApi::class)
    public sealed class Reusable() : Slot {
        protected val mutex: Mutex = Mutex()
        /** Внутреннее хранение текущей версии слота */
        protected val version: AtomicInt = AtomicInt(0)

        /** Текущая версия слота */
        public val currentVersion: Int
            get() = version.load()

        /**
         * Извлечение ресурса из слота, если имеется, в количестве не менее требуемого.
         *
         * @param requestQuantity требуемое количество
         * @return [Result] ресурса с версией [ResultWithVersion]
         * */
        public abstract suspend fun pull(requestQuantity: Long): Result<ResultWithVersion>

        /**
         * Добавление ресурса в слот.
         *
         * @param resource добавляемый ресурс
         * @return [Result] новая версия изменившегося слота
         * */
        public abstract suspend fun add(resource: String): Result<Int>
    }

    /**
     * Поштучное хранение с общим сроком годности. Хранение виртуальное — хранится только
     * информация о сврйствах ресурса и размере слота. При извлечении ресурса методом [pull] проверяется
     * текущий размер слота и, если он не меньше запрашиваемого количества, — то генерация ресурса и
     * соответсвующее уменьшение текущего размера слота. Генерация производится из свойств
     * [macronutrients], [expiration] и отпущенного количества.
     *
     * @param macronutrients свойства ресурса
     * @param expiration срок годности
     * @param capacity максимальная вместимость слота
     * @param dispatcher необходим для подмены в Unit-тестах
     * */
    public class Piece(
        macronutrients: String,
        expiration: Instant,
        capacity: Int,
        override val dispatcher: CoroutineDispatcher = Dispatchers.Default
    ) : Disposable(macronutrients, expiration, capacity) {

        private var _size: Int = capacity

        override suspend fun size(): Int {
            check(isActive) { INACTIVE_MESSAGE }
            return withContext(dispatcher) {
                check(isActive) { INACTIVE_MESSAGE }
                mutex.withLock { _size }
            }
        }

        override suspend fun pull(requestQuantity: Number): Result<String> {
            check(isActive) { INACTIVE_MESSAGE }
            return withContext(dispatcher) {
                // Sic! Двойная проверка для строгости
                check(isActive) { INACTIVE_MESSAGE }
                mutex.withLock {
                    (_size - requestQuantity.toInt()).takeIf { it >= 0 }
                        ?.also { _size = it }
                        ?.let {
                            // Реально отпускаемое количество может не соответствовать запрашиваемому.
                            // Здесь можно подменить это значение и потом решать вопрос с ревизией и ОБХСС :)
                            val realQuantity = requestQuantity
                            Result.success(
                                buildResponse(macronutrients, realQuantity, expiration)
                            )
                        } ?: emptySlot()
                }
            }
        }
    }

    /** Весовое хранение с общим сроком годности. Хранение виртуальное — хранится только
     * информация о сврйствах ресурса и размере слота. При извлечении ресурса методом [pull] проверяется
     * текущий размер слота и, если он не меньше запрашиваемого количества, — то генерация ресурса и
     * соответсвующее уменьшение текущего размера слота. Генерация производится из свойств
     * [macronutrients], [expiration] и отпущенного количества.
     *
     * @param macronutrients свойства ресурса
     * @param expiration срок годности
     * @param capacity максимальная вместимость слота
     * @param dispatcher необходим для подмены в Unit-тестах
     * */
    public class Weight(
        macronutrients: String,
        expiration: Instant,
        capacity: Long,
        override val dispatcher: CoroutineDispatcher = Dispatchers.Default
    ) : Disposable(macronutrients, expiration, capacity) {

        private var _size: Long = capacity

        override suspend fun size(): Long {
            check(isActive) { INACTIVE_MESSAGE }
            return withContext(dispatcher) {
                check(isActive) { INACTIVE_MESSAGE }
                mutex.withLock { _size }
            }
        }

        override suspend fun pull(requestQuantity: Number): Result<String> {
            check(isActive) { INACTIVE_MESSAGE }
            return withContext(dispatcher) {
                // Sic! Двойная проверка для строгости
                check(isActive) { INACTIVE_MESSAGE }
                mutex.withLock {
                    (_size - requestQuantity.toLong()).takeIf { it >= 0 }
                        ?.also { _size = it }
                        ?.let {
                            // Реально отпускаемое количество может не соответствовать запрашиваемому.
                            // Усушка, утруска, обвес и прочая складская магия :)
                            val realQuantity = requestQuantity
                            Result.success(
                                buildResponse(macronutrients, realQuantity, expiration)
                            )
                        } ?: emptySlot()
                }
            }
        }
    }

    /**
     * Поштучное хранение ресурса в упаковке со своими свойствами срока годности и весом/кол-вом.
     * Хранение реальное в колекции [ArrayDeque].
     *
     * @param capacity максимальная вместимость слота
     * @param dispatcher необходим для подмены в Unit-тестах
     * */
    @OptIn(ExperimentalAtomicApi::class)
    public class Pack(
        private val capacity: Int,
        override val dispatcher: CoroutineDispatcher = Dispatchers.Default
    ) : Reusable() {

        private val slot = ArrayDeque<String>(capacity)

        override suspend fun size(): Int = withContext(dispatcher) {
            mutex.withLock { slot.size }
        }

        override suspend fun pull(requestQuantity: Long): Result<ResultWithVersion> =
            withContext(dispatcher) {
                mutex.withLock {
                    // Найти подходящий ресурс в слоте по критерию веса — не менее запрашиваемому
                    // и извлечь его из слота
                    slot.firstOrNull { it quantityIsNotLessThan requestQuantity }?.let { el ->
                        Result.success(version.incrementAndFetch() to el)
                            .also { slot.remove(el) }
                    } ?: Result.failure((SlotError("Slot is empty")))
                }
            }

        override suspend fun add(resource: String): Result<Int> = withContext(dispatcher) {
            mutex.withLock {
                resource.takeIf { slot.size < capacity }
                    ?.also(slot::add)
                    ?.let { Result.success(version.incrementAndFetch()) }
                    ?: slotOverflow()
            }
        }

        /**
         * Поиск ресурса с требуемым количеством.
         *
         * @receiver строковое представление ресурса
         * @param request запрашиваемое количество
         * @return результат поиска
         * */
        private infix fun String.quantityIsNotLessThan(request: Long): Boolean =
            FIND_QUANTITY_RE.find(this)?.groupValues?.get(1)?.toLongOrNull()
                ?.let { it >= request } ?: false

        override fun toString(): String =
            "[capacity: $capacity, version: $currentVersion, dispatcher: $dispatcher]"

        private companion object {
            val FIND_QUANTITY_RE = """"quantity"\s*:\s*(\d+)""".toRegex()
        }
    }

    public companion object {
        private const val MACRONUTRIENTS_PLACEHOLDER = "{macronutrients}"
        private const val QUANTITY_PLACEHOLDER = "{quantity}"
        private const val EXPIRATION_PLACEHOLDER = "{expiration}"

        private const val RESOURCE_TEMPLATE = """{
    "macronutrients": $MACRONUTRIENTS_PLACEHOLDER,
    "quantity": $QUANTITY_PLACEHOLDER,
    "expiration": "$EXPIRATION_PLACEHOLDER"
}"""

        private const val INACTIVE_MESSAGE = "RIP. I'm dead. Forget me forever :("

        /**
         *  Генерация элемента хранения в соответствии с параметрами слота, где он хранится.
         *  Полагается, что слоты хранят элементы в виде строки (JSON).
         *  Реальное хранение заменяется генерацией элемента .
         */
        public fun buildResponse(macronutrients: String, quantity: Number, expiration: Instant): String =
            RESOURCE_TEMPLATE.replace(MACRONUTRIENTS_PLACEHOLDER, macronutrients)
                .replace(QUANTITY_PLACEHOLDER, quantity.toString())
                .replace(EXPIRATION_PLACEHOLDER, expiration.toString())
    }
}
