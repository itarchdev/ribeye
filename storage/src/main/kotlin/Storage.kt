package ru.it_arch.tools.samples.ribeye.storage

import kotlinx.coroutines.sync.Mutex
import ru.it_arch.tools.samples.ribeye.ResourceRepository
import ru.it_arch.tools.samples.ribeye.data.Quantity
import ru.it_arch.tools.samples.ribeye.data.Resource
import ru.it_arch.tools.samples.ribeye.storage.impl.toGrill
import ru.it_arch.tools.samples.ribeye.storage.impl.toMeat
import ru.it_arch.tools.samples.ribeye.storage.impl.toRosemary
import ru.it_arch.tools.samples.ribeye.storage.impl.toSauceIngredients
import ru.it_arch.tools.samples.ribeye.storage.impl.format
import ru.it_arch.tools.samples.ribeye.storage.slot.Slot
import kotlin.reflect.KClass

public class Storage(
    private val meatSlotCapacity: Quantity.Piece,
    private val grillSlotCapacity: Quantity.Weight,
    private val sauceSlotCapacity: Quantity.Weight,
    private val rosemarySlotCapacity: Quantity.Piece
) : ResourceRepository {

    private val mutex = Mutex()
    /** Ленивая инициализация слота при добавлении ресурса */
    private val slots: MutableMap<KClass<out Resource>, Slot> = mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T : Resource, Q : Quantity> getByType(
        type: KClass<out T>,
        requestQuantity: Q
    ): Result<T> =
        when (type) {
            Resource.Meat::class -> slots[type]?.takeIf { it is Slot.Pack }
                ?.let { slot -> (slot as Slot.Pack).get(requestQuantity.toString())
                    .map { it.second.toMeat() as T }
                } ?: notFound(type)

            Resource.Grill::class -> slots[type]?.takeIf { it is Slot.Weight }
                ?.let { slot -> (slot as Slot.Weight).get(requestQuantity.toString())
                    .map { it.toGrill() as T }
                } ?: notFound(type)

            Resource.SauceIngredients::class -> slots[type]?.takeIf { it is Slot.Weight }
                ?.let { slot -> (slot as Slot.Weight).get(requestQuantity.toString())
                    .map { it.toSauceIngredients() as T }
                } ?: notFound(type)

            Resource.Rosemary::class -> slots[type]?.takeIf { it is Slot.Piece }
                ?.let { slot -> (slot as Slot.Piece).get(requestQuantity.toString())
                    .map { it.toRosemary() as T }
                } ?: notFound(type)

            else -> error("Unknown resource type: ${type.simpleName}")
        }

    override suspend fun <T : Resource> putByType(type: KClass<out T>, resource: T): Result<Unit> =
        when(type) {
            // Используем имеющийся слот, если есть
            Resource.Meat::class -> (resource as Resource.Meat)
                .let { meat ->
                    (slots[type]?.takeIf { it is Slot.Pack }?.let { it as Slot.Pack } ?: run {
                        Slot.Pack(meatSlotCapacity.boxed).also { slots[type] = it }
                    }).add(meat.format()).map { Unit }
                }
            // старое выкидываем, создавая новый слот под новый срок хранения
            Resource.Grill::class -> (resource as Resource.Grill)
                .let { grill ->
                    Slot.Weight(
                        grill.macronutrients.format(),
                        grill.expiration.boxed,
                        grillSlotCapacity.boxed
                    )
                }
                .also { slots[type] = it }
                .let { Result.success(Unit) }

            Resource.SauceIngredients::class -> (resource as Resource.SauceIngredients)
                .let { sauce ->
                    Slot.Weight(
                        sauce.macronutrients.format(),
                        sauce.expiration.boxed,
                        sauceSlotCapacity.boxed
                    )
                }
                .also { slots[type] = it }
                .let { Result.success(Unit) }

            Resource.Rosemary::class -> (resource as Resource.Rosemary)
                .let { rosemary ->
                    Slot.Piece(
                        rosemary.macronutrients.format(),
                        rosemary.expiration.boxed,
                        rosemarySlotCapacity.boxed
                    )
                }
                .also { slots[type] = it }
                .let { Result.success(Unit) }

            else -> error("Unknown resource type: ${type.simpleName}")
        }
}
