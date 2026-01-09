package ru.it_arch.tools.samples.ribeye.storage

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.it_arch.tools.samples.ribeye.ResourceRepository
import ru.it_arch.tools.samples.ribeye.data.Quantity
import ru.it_arch.tools.samples.ribeye.data.Resource
import ru.it_arch.tools.samples.ribeye.storage.impl.toGrill
import ru.it_arch.tools.samples.ribeye.storage.impl.toMeat
import ru.it_arch.tools.samples.ribeye.storage.impl.toRosemary
import ru.it_arch.tools.samples.ribeye.storage.impl.toSauceIngredients
import ru.it_arch.tools.samples.ribeye.storage.impl.format
import ru.it_arch.tools.samples.ribeye.storage.slot.Slot
import kotlin.math.pow
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.milliseconds

public class Storage(
    private val meatSlotCapacity: Quantity.Piece,
    private val grillSlotCapacity: Quantity.Weight,
    private val sauceSlotCapacity: Quantity.Weight,
    private val rosemarySlotCapacity: Quantity.Piece
) : ResourceRepository {

    private val mutex = Mutex()
    /** Ленивая инициализация слота при добавлении ресурса */
    private val _slots: MutableMap<KClass<out Resource>, Slot> = mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    private suspend fun <S : Slot> getSlot(type: KClass<out Resource>): S? = mutex.withLock {
        _slots[type]?.let { it as S }
    }

    private suspend fun setSlot(type: KClass<out Resource>, slot: Slot) {
        mutex.withLock { _slots[type] = slot }
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T : Resource, Q : Quantity> getByType(
        type: KClass<out T>,
        requestQuantity: Q
    ): Result<T> = when (type) {
        /* Переиспользуемый слот, который может быть в данный момент занят другим потоком.
        Делаются 3 попытки захвата с проверкой версии и экспоненциальной задержкой между попытками */
        Resource.Meat::class -> getSlot<Slot.Pack>(type)?.let { slot ->
            repeat(ATTEMPTS) { attempt ->
                // 1. Capture the version BEFORE starting any work
                val startVersion = slot.currentVersion

                try {
                    slot.get(requestQuantity.toString()).map { (version, res) ->

                        // 3. OPTIMISTIC CHECK:
                        // In this case, since the manager itself increments the version
                        // during the atomic mutex block, 'newVersion' must be exactly
                        // 'startVersion + 1' if no one else touched it.
                        if (version == startVersion + 1) {
                            res.toMeat() as T
                        } else {
                            throw StaleSlotVersion("Concurrent modification detected")
                        }


                    }.onFailure { return@let notFound(type) }

                } catch (_: StaleSlotVersion) {
                    if (attempt < (ATTEMPTS - 1)) delay(attempt.nextDelay())
                }

                getSlot<Slot.Pack>(type)?.let { slot ->
                    slot.get(requestQuantity.toString()).map { it.second.toMeat() as T }
                } ?: notFound(type)
            }

            notFound(type)

        } ?: notFound(type)

        Resource.Grill::class -> getSlot<Slot.Weight>(type)?.let { slot ->
            slot.get(requestQuantity.toString()).map { it.toGrill() as T }
        } ?: notFound(type)

        Resource.SauceIngredients::class -> getSlot<Slot.Weight>(type)?.let { slot ->
            slot.get(requestQuantity.toString()).map { it.toSauceIngredients() as T }
        } ?: notFound(type)

        Resource.Rosemary::class -> getSlot<Slot.Piece>(type)?.let { slot ->
            slot.get(requestQuantity.toString()).map { it.toRosemary() as T }
        } ?: notFound(type)

        else -> error("Unknown resource type: ${type.simpleName}")
    }

    override suspend fun <T : Resource> putByType(type: KClass<out T>, resource: T): Result<Unit> =
        when(type) {
            // Используем имеющийся слот, если есть
            Resource.Meat::class -> (resource as Resource.Meat).let { meat ->
                (getSlot(type) ?: run {
                    Slot.Pack(meatSlotCapacity.boxed).also { setSlot(type, it) }
                }).add(meat.format()).map { Unit }
            }
            // старое выкидываем, создавая новый слот под новый срок хранения
            Resource.Grill::class -> (resource as Resource.Grill).let { grill ->
                Slot.Weight(
                    grill.macronutrients.format(),
                    grill.expiration.boxed,
                    grillSlotCapacity.boxed
                )
            }.also { setSlot(type, it) }.let { Result.success(Unit) }

            Resource.SauceIngredients::class -> (resource as Resource.SauceIngredients).let { sauce ->
                Slot.Weight(
                    sauce.macronutrients.format(),
                    sauce.expiration.boxed,
                    sauceSlotCapacity.boxed
                )
            }.also { setSlot(type, it) }.let { Result.success(Unit) }

            Resource.Rosemary::class -> (resource as Resource.Rosemary).let { rosemary ->
                Slot.Piece(
                    rosemary.macronutrients.format(),
                    rosemary.expiration.boxed,
                    rosemarySlotCapacity.boxed
                )
            }.also { setSlot(type, it) }.let { Result.success(Unit) }

            else -> error("Unknown resource type: ${type.simpleName}")
        }

    public companion object {
        private const val ATTEMPTS = 3

        /** Расчет экспонентциальной задержки */
        private fun Int.nextDelay() = 100.milliseconds * 2.0.pow(this)
    }
}
