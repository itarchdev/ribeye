package ru.it_arch.tools.samples.ribeye.storage

import ru.it_arch.tools.samples.ribeye.ResourceRepository
import ru.it_arch.tools.samples.ribeye.data.Quantity
import ru.it_arch.tools.samples.ribeye.data.Resource
import ru.it_arch.tools.samples.ribeye.storage.impl.grillFromJson
import ru.it_arch.tools.samples.ribeye.storage.impl.meatFromJson
import ru.it_arch.tools.samples.ribeye.storage.impl.rosemaryFromJson
import ru.it_arch.tools.samples.ribeye.storage.impl.sauceFromJson
import ru.it_arch.tools.samples.ribeye.storage.impl.toJson
import kotlin.reflect.KClass

public class Storage(
    private val meatSlotCapacity: Quantity.Piece,
    private val grillSlotCapacity: Quantity.Weight,
    private val sauceSlotCapacity: Quantity.Weight,
    private val rosemarySlotCapacity: Quantity.Piece
) : ResourceRepository {

    /** Ленивая инициализация слота при добавлении ресурса */
    private val slots: MutableMap<KClass<out Resource>, Slot> = mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    override fun <T : Resource, Q : Quantity> getByType(type: KClass<out T>, requestQuantity: Q): Result<T> =
        when(type) {
            Resource.Meat::class -> slots[type]?.takeIf { it is Slot.Pack }
                ?.let { slot -> slot.get(requestQuantity.toString()).map { it.meatFromJson() as T } }
                ?: notFound(type)
            Resource.Grill::class -> slots[type]?.takeIf { it is Slot.Weight }
                ?.let { slot -> slot.get(requestQuantity.toString()).map { it.grillFromJson() as T } }
                ?: notFound(type)
            Resource.SauceIngredients::class -> slots[type]?.takeIf { it is Slot.Weight }
                ?.let { slot -> slot.get(requestQuantity.toString()).map { it.sauceFromJson() as T } }
                ?: notFound(type)
            Resource.Rosemary::class -> slots[type]?.takeIf { it is Slot.Piece }
                ?.let { slot -> slot.get(requestQuantity.toString()).map { it.rosemaryFromJson() as T } }
                ?: notFound(type)
            else -> error("Unknown resource type: ${type.simpleName}")
        }

    // always new slot ?
    override fun <T : Resource> putByType(type: KClass<out T>, resource: T): Result<Unit> =
        when(type) {
            // Используем имеющийся слот, если есть
            Resource.Meat::class -> (resource as Resource.Meat)
                .let { meat ->
                    (slots[type]?.takeIf { it is Slot.Pack }?.let { it as Slot.Pack } ?: run {
                        Slot.Pack(meatSlotCapacity.boxed).also { slots[type] = it }
                    }).add(meat.toJson())
                }
            // старое выкидываем, создавая новый слот под новый срок хранения
            Resource.Grill::class -> (resource as Resource.Grill)
                .let { grill ->
                    Slot.Weight(
                        grill.macronutrients.toJson(),
                        grill.expiration.toString(),
                        grillSlotCapacity.boxed
                    )
                }
                .also { slots[type] = it }
                .let { Result.success(Unit) }
            Resource.SauceIngredients::class -> (resource as Resource.SauceIngredients)
                .let { sauce ->
                    Slot.Weight(
                        sauce.macronutrients.toJson(),
                        sauce.expiration.toString(),
                        sauceSlotCapacity.boxed
                    )
                }
                .also { slots[type] = it }
                .let { Result.success(Unit) }
            Resource.Rosemary::class -> (resource as Resource.Rosemary)
                .let { rosemary ->
                    Slot.Piece(
                        rosemary.macronutrients.toJson(),
                        rosemary.expiration.toString(),
                        rosemarySlotCapacity.boxed
                    )
                }
                .also { slots[type] = it }
                .let { Result.success(Unit) }
            else -> error("Unknown resource type: ${type.simpleName}")
        }
}
