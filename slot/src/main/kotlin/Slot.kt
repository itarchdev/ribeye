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
 * Типонезависимый контейнер (слот) хранилища для разных видов ресурсов — упакованных, штучных, весовых.
 * Внутренее представление — строковое, по факту — хардкодинг в JSON-формате. Однако контейнер
 * не должен знать о фактическом формате представления его элементов. Сериализация/десериализация
 * осуществляется уровнем выше.
 * */
public sealed interface Slot {

    /** Вынесен с целью замены для Unit-тестов. По умолчанию — [Dispatchers.Default] */
    public val dispatcher: CoroutineDispatcher

    /** текущая заполненность слота */
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

        public fun kill() {
            isActive = false
        }

        /**
         *
         * @param requestQuantity
         * @return
         * */
        public abstract suspend fun get(requestQuantity: String): Result<String>
    }

    /**
     * Переиспользуемый слот. Состояние ЖЦ определяется сквозной версией [currentVersion]
     *
     *
     * */
    @OptIn(ExperimentalAtomicApi::class)
    public sealed class Reusable() : Slot {
        protected val mutex: Mutex = Mutex()
        protected val version: AtomicInt = AtomicInt(0)
        public val currentVersion: Int
            get() = version.load()

        public abstract suspend fun get(requestQuantity: String): Result<Pair<Int, String>>

        public abstract suspend fun add(resource: String): Result<Int>
    }

    /**
     * Поштучное хранение с общим сроком годности.
     * */
    public class Piece(
        macronutrients: String,
        expiration: Instant,
        capacity: Int,
        override val dispatcher: CoroutineDispatcher = Dispatchers.Default
    ) : Disposable(macronutrients, expiration, capacity) {

        private var _size: Int = capacity

        override suspend fun size(): Int = withContext(dispatcher) {
            check(isActive) { INACTIVE_MESSAGE }
            mutex.withLock { _size }
        }

        override suspend fun get(requestQuantity: String): Result<String> {
            check(isActive) { INACTIVE_MESSAGE }
            return withContext(dispatcher) {
                // Sic! Двойная проверка для строгости
                check(isActive) { INACTIVE_MESSAGE }
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
    }

    /** Весовое хранение с общим сроком годности. */
    public class Weight(
        macronutrients: String,
        expiration: Instant,
        capacity: Long,
        override val dispatcher: CoroutineDispatcher = Dispatchers.Default
    ) : Disposable(macronutrients, expiration, capacity) {

        private var _size: Long = capacity

        override suspend fun size(): Long = withContext(dispatcher) {
            mutex.withLock { _size }
        }

        override suspend fun get(requestQuantity: String): Result<String> {
            check(isActive) { INACTIVE_MESSAGE }
            return withContext(dispatcher) {
                // Sic! Двойная проверка для строгости
                check(isActive) { INACTIVE_MESSAGE }
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
    }

    /** Поштучное хранение ресурса в упаковке со своим сроком годности и весом/кол-вом. */
    @OptIn(ExperimentalAtomicApi::class)
    public class Pack(
        private val capacity: Int,
        override val dispatcher: CoroutineDispatcher = Dispatchers.Default
    ) : Reusable() {

        private val slot = ArrayDeque<String>(capacity)

        override suspend fun size(): Int = withContext(dispatcher) {
            mutex.withLock { slot.size }
        }

        /**
         * Получение
         * */
        override suspend fun get(requestQuantity: String): Result<Pair<Int, String>> =
            withContext(dispatcher) {
                mutex.withLock {
                    // Найти подходящий ресурс в слоте по критерию веса — большего или равным запрашиваемому
                    // и извлечь его из слота
                    requestQuantity.toIntOrNull()?.let { intQuantity ->
                        slot.firstOrNull { it quantityIsNotLessThan intQuantity }
                            ?.let { el ->
                                Result.success(version.incrementAndFetch() to el)
                                    .also { slot.remove(el) }
                            } ?: Result.failure((SlotError("Slot is empty")))
                    } ?: error("request quantity must be Int")
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
         * @receiver строковое представление ресурса
         * @param request запрашиваемое количество
         * @return результат поиска
         * */
        private infix fun String.quantityIsNotLessThan(request: Int): Boolean =
            FIND_QUNTITY_RE.find(this)?.groupValues?.get(1)?.toIntOrNull()?.let { it >= request }
                ?: false

        private companion object {
            val FIND_QUNTITY_RE = """"quantity"\s*:\s*(\d+)""".toRegex()
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
        public fun buildResponse(macronutrients: String, quantity: String, expiration: Instant): String =
            RESOURCE_TEMPLATE.replace(MACRONUTRIENTS_PLACEHOLDER, macronutrients)
                .replace(QUANTITY_PLACEHOLDER, quantity)
                .replace(EXPIRATION_PLACEHOLDER, expiration.toString())
    }
}
