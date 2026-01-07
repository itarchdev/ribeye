package ru.it_arch.tools.samples.ribeye.storage

import ru.it_arch.tools.samples.ribeye.ResourceRepository
import ru.it_arch.tools.samples.ribeye.data.Quantity
import ru.it_arch.tools.samples.ribeye.data.Resource
import ru.it_arch.tools.samples.ribeye.storage.impl.toJson
import kotlin.reflect.KClass

public class Storage(
    private val meatSlotCapacity: Int,
    private val grillSlotCapacity: Long,
    private val sauceSlotCapacity: Long,
    private val rosemarySlotCapacity: Int
) : ResourceRepository {

    /** Ленивая инициализация слота при добавлении ресурса */
    private val slots: MutableMap<KClass<out Resource>, Slot> = mutableMapOf()

    override fun <T : Resource> getByType(type: KClass<out T>, requestQuantity: Quantity): Result<T> {
        TODO("Not yet implemented")
    }

    // always new slot ?
    override fun <T : Resource> putByType(type: KClass<out T>, resource: T): Result<Unit> =
        when(type) {
            // Используем имеющийся слот, если есть
            Resource.Meat::class -> (resource as Resource.Meat)
                .let { meat ->
                    (slots[type]?.takeIf { it is Slot.Pack }?.let { it as Slot.Pack } ?: run {
                        Slot.Pack(meatSlotCapacity).also { slots[type] = it }
                    }).add(meat.toJson())
                }
            // старое выкидываем, создавая новый слот под новый срок хранения
            Resource.Grill::class -> (resource as Resource.Grill)
                .let { grill ->
                    Slot.Weight(
                        grill.macronutrients.toJson(),
                        grill.expiration.toString(),
                        grillSlotCapacity
                    )
                }
                .also { slots[type] = it }
                .let { Result.success(Unit) }
            Resource.SauceIngredients::class -> (resource as Resource.SauceIngredients)
                .let { sauce ->
                    Slot.Weight(
                        sauce.macronutrients.toJson(),
                        sauce.expiration.toString(),
                        sauceSlotCapacity
                    )
                }
                .also { slots[type] = it }
                .let { Result.success(Unit) }
            Resource.Rosemary::class -> (resource as Resource.Rosemary)
                .let { rosemary ->
                    Slot.Piece(
                        rosemary.macronutrients.toJson(),
                        rosemary.expiration.toString(),
                        rosemarySlotCapacity
                    )
                }
                .also { slots[type] = it }
                .let { Result.success(Unit) }
            else -> error("Unknown resource type: ${type.simpleName}")
        }
}
