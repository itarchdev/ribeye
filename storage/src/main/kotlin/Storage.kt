package ru.it_arch.tools.samples.ribeye.storage

import ru.it_arch.tools.samples.ribeye.ResourceRepository
import ru.it_arch.tools.samples.ribeye.data.Quantity
import ru.it_arch.tools.samples.ribeye.data.Resource
import ru.it_arch.tools.samples.ribeye.storage.impl.QuantityPieceImpl
import kotlin.reflect.KClass

public class Storage(

) : ResourceRepository {

    /** Ленивая инициализация слота при добавлении ресурса */
    private val slots: MutableMap<KClass<out Resource>, Slot<Resource, out Quantity>> =
        mutableMapOf()
    /*
    private val meatSlot = SlotPack<SlotData.Packed.Meat>(QuantityPieceImpl(10))
    private val grillSlot = SlotWeight(
        ExpirationImpl(Instant.DISTANT_FUTURE),
        QuantityWeightImpl(1_000_000L),
        SlotData.Unpacked.Grill()
    )
    private val sauceSlot = SlotWeight(
        ExpirationImpl(Instant.DISTANT_FUTURE),
        QuantityWeightImpl(1_000L),
        SlotData.Unpacked.SauceComponents()
    )
    private val rosemarySlot = SlotPiece(

    )*/

    override fun <T : Resource> getByType(type: KClass<out T>, requestQuantity: Quantity): Result<T> {
        TODO("Not yet implemented")
    }

    // always new slot ?
    override fun <T : Resource> putByType(type: KClass<out T>, resource: T): Result<Unit> =
        when(type) {
            // Используем имеющийся слот, если есть
            Resource.Meat::class -> (resource as Resource.Meat)
                .let {
                    (slots[type]?.takeIf { it is Slot.Pack }?.let { it as Slot.Pack } ?: run {
                        Slot.Pack<Resource>(QuantityPieceImpl(10))
                            .also { slots[type] = it }
                    }).add(resource)
                }
            // старое выкидываем, создавая новый слот под новый срок хранения
            Resource.Grill::class -> (resource as Resource.Grill)
                .let { Slot.Weight(it.macronutrients, it.expiration, /*it.quantity*/) } // Max!!!
                .also { slots[type] = it }
                .let { Result.success(Unit) }
            Resource.SauceIngredients::class -> (resource as Resource.SauceIngredients)
                .let { Slot.Weight(it.macronutrients, it.expiration, it.quantity) }
                .also { slots[type] = it }
                .let { Result.success(Unit) }
            Resource.Rosemary::class -> (resource as Resource.Rosemary)
                .let { Slot.Piece(it.macronutrients, it.expiration, it.quantity) }
                .also { slots[type] = it }
                .let { Result.success(Unit) }
            else -> error("Unknown resource type: ${type.simpleName}")
        }
}
