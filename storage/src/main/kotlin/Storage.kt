package ru.it_arch.tools.samples.ribeye.storage

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.it_arch.tools.samples.ribeye.ResourceRepository
import ru.it_arch.tools.samples.ribeye.data.Quantity
import ru.it_arch.tools.samples.ribeye.data.Resource
import ru.it_arch.tools.samples.ribeye.storage.impl.QuantityPieceImpl
import ru.it_arch.tools.samples.ribeye.storage.impl.QuantityWeightImpl
import ru.it_arch.tools.samples.ribeye.storage.impl.toGrill
import ru.it_arch.tools.samples.ribeye.storage.impl.toMeat
import ru.it_arch.tools.samples.ribeye.storage.impl.toRosemary
import ru.it_arch.tools.samples.ribeye.storage.impl.toSauceIngredients
import ru.it_arch.tools.samples.ribeye.storage.impl.format
import ru.it_arch.tools.samples.ribeye.storage.slot.Slot
import kotlin.math.pow
import kotlin.reflect.KClass
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

/**
 *
 *
 * @param slots для тестовых целей
 * */
public class Storage(
    private val slotFactory: SlotFactory,
    slots: Map<KClass<out Resource>, Slot>? = null
) : ResourceRepository {

    private val mutex = Mutex()
    /** Ленивая инициализация слота при добавлении ресурса */
    private val _slots: MutableMap<KClass<out Resource>, Slot> =
        slots?.toMutableMap() ?: mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    private suspend fun <S : Slot> getSlot(type: KClass<out Resource>): S? = mutex.withLock {
        _slots[type]?.let { it as S }
    }

    private suspend fun setSlot(type: KClass<out Resource>, slot: Slot) {
        mutex.withLock {
            _slots[type]?.also { if (it is Slot.Disposable) it.kill() }
            _slots[type] = slot
        }
    }

    /**
     * Извлчение ресурса с использованием оптимистичной блокировки.
     * Делается несколько попыток захвата слота с проверкой версии и
     * [экспоненциальной задержкой](https://en.wikipedia.org/wiki/Exponential_backoff) между попытками.
     *
     * @param T тип ресурса [Resource]
     * @param type [KClass] типа ресурса [Resource]
     * @param slot [Slot.Pack] слот, из которого забирается ресурс
     * @param requestQuantity требуемое количество
     * @return [Result] конкретного ресурса [Resource] или ошибка получения
     *  */
    private suspend fun <T : Resource> pullWithOptimisticLocking(
        type: KClass<out T>,
        slot: Slot.Reusable,
        requestQuantity: Quantity.Weight
    ): Result<T> {
        repeat(ATTEMPTS) { attempt ->
            // Фиксация версии прежде, чем что-то делать
            val startVersion = slot.currentVersion

            @Suppress("UNCHECKED_CAST")
            try {
                slot.pull(requestQuantity.boxed).onSuccess { (version, res) ->
                    /* Оптимистическая проверка:
                    так как слот увеличивает номер версии внутри атомарного блока мьютекса при
                    вызове, значение version должно быть строго равно startVersion + 1,
                    если никто другой его не изменял */
                    return if (version == startVersion + 1)
                        Result.success(res.toMeat() as T)
                    else
                        throw StaleSlotVersionError("Concurrent modification detected. Resource: ${type.simpleName}, slot: $slot, start version: $startVersion")
                }.onFailure { return notFound(type) }
            } catch (_: StaleSlotVersionError) {
                if (attempt < (ATTEMPTS - 1)) delay(attempt.nextDelay())
            }
        }
        return Result.failure(StorageError.ExhaustedRetries(slot::class.simpleName.orEmpty()))
    }

    private suspend fun <T : Resource> setWithOptimisticLocking(
        type: KClass<out T>,
        slot: Slot.Reusable,
        resource: String
    ) : Result<Int> {
        repeat(ATTEMPTS) { attempt ->
            // Фиксация версии прежде, чем что-то делать
            val startVersion = slot.currentVersion
            try {
                slot.add(resource).onSuccess { version ->
                    /* Оптимистическая проверка:
                    если никто не изменил слот, то версия будет инкрементирована функцией slot.add */
                    return if (version == startVersion + 1) Result.success(version) else
                        throw StaleSlotVersionError("Concurrent modification detected. Resource: ${type.simpleName}, slot: $slot, start version: $startVersion")
                }.onFailure { return Result.failure(it) }
            } catch (_: StaleSlotVersionError) {
                if (attempt < (ATTEMPTS - 1)) delay(attempt.nextDelay())
            }
        }
        return Result.failure(StorageError.ExhaustedRetries(slot::class.simpleName.orEmpty()))
    }

    /**
     * Слоты [Slot.Disposable] могут выкинуть исключение [IllegalStateException] при проверке
     * своего состояния; в этом случае возвращаем 0.
     */
    @Suppress("UNCHECKED_CAST")
    override suspend fun <T : Resource, Q : Quantity> sizeByType(type: KClass<out T>): Q =
        when(type) {
            Resource.Meat::class -> (getSlot<Slot.Reusable>(type)?.size() ?: 0)
                .let { QuantityPieceImpl(it.toInt()) as Q }

            Resource.Grill::class, Resource.SauceIngredients::class ->
                (getSlot<Slot.Disposable>(type)?.let { slot ->
                    try { slot.size().toLong() }
                    catch (_: IllegalStateException) { 0L }
                } ?: 0L).let { QuantityWeightImpl(it) as Q }

            Resource.Rosemary::class ->
                (getSlot<Slot.Disposable>(type)?.let { slot ->
                    try { slot.size().toInt() }
                    catch (_: IllegalStateException) { 0 }
                } ?: 0).let { QuantityPieceImpl(it) as Q }

            else -> error("Unknown resource type: ${type.simpleName}")
        }

    /**
     * Слоты [Slot.Disposable] могут выкинуть исключение [IllegalStateException] при проверке
     * своего состояния; в этом случае возвращаем notFound.
     */
    @Suppress("UNCHECKED_CAST")
    override suspend fun <T : Resource, Q : Quantity> pullByType(
        type: KClass<out T>,
        requestQuantity: Q
    ): Result<T> = when(type) {
        // Переиспользуемый слот, который может быть в данный момент занят другим потоком.
        Resource.Meat::class -> getSlot<Slot.Pack>(type)
            ?.let { pullWithOptimisticLocking(type, it, (requestQuantity as Quantity.Weight)) }
            ?: notFound(type)

        Resource.Grill::class -> getSlot<Slot.Weight>(type)?.let { slot ->
            try {
                slot.pull((requestQuantity as Quantity.Weight).boxed)
                    .map { it.toGrill() as T }
            } catch (_: IllegalStateException) { null }
        } ?: notFound(type)

        Resource.SauceIngredients::class -> getSlot<Slot.Weight>(type)?.let { slot ->
            try {
                slot.pull((requestQuantity as Quantity.Weight).boxed)
                    .map { it.toSauceIngredients() as T }
            } catch (_: IllegalStateException) { null }
        } ?: notFound(type)

        Resource.Rosemary::class -> getSlot<Slot.Piece>(type)?.let { slot ->
            try {
                slot.pull((requestQuantity as Quantity.Piece).boxed)
                    .map { it.toRosemary() as T }
            } catch (_: IllegalStateException) { null }
        } ?: notFound(type)

        else -> error("Unknown resource type: ${type.simpleName}")
    }

    override suspend fun <T : Resource> putByType(type: KClass<out T>, resource: T): Result<Unit> =
        resource.takeUnless { it.isRotten() }?.let {
            when(type) {
                // Используем имеющийся слот, если есть
                Resource.Meat::class -> (resource as Resource.Meat).let { meat ->
                    (getSlot(type) ?: run {
                        slotFactory.slotForMeat().also { setSlot(type, it) }
                    }).let { slot ->
                        setWithOptimisticLocking(type, slot, meat.format()).map { Unit }
                    }
                }
                // старое выкидываем, создавая новый слот под новый срок хранения
                Resource.Grill::class -> (resource as Resource.Grill).let { grill ->
                    slotFactory.slotForGrill(grill.macronutrients, grill.expiration)
                }.also { setSlot(type, it) }.let { Result.success(Unit) }

                Resource.SauceIngredients::class -> (resource as Resource.SauceIngredients).let { sauce ->
                    slotFactory.slotForSauce(sauce.macronutrients, sauce.expiration)
                }.also { setSlot(type, it) }.let { Result.success(Unit) }

                Resource.Rosemary::class -> (resource as Resource.Rosemary).let { rosemary ->
                    slotFactory.slotForRosemary(rosemary.macronutrients, rosemary.expiration)
                }.also { setSlot(type, it) }.let { Result.success(Unit) }

                else -> error("Unknown resource type: ${type.simpleName}")
            }
        } ?: resource.rotten()

    public companion object {
        private const val ATTEMPTS = 3

        /** Расчет экспонентциальной задержки */
        private fun Int.nextDelay() = 100.milliseconds * 2.0.pow(this)
    }
}
