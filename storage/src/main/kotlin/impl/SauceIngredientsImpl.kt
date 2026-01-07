package ru.it_arch.tools.samples.ribeye.storage.impl

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import ru.it_arch.k3dm.ValueObject
import ru.it_arch.tools.samples.ribeye.data.Expiration
import ru.it_arch.tools.samples.ribeye.data.Macronutrients
import ru.it_arch.tools.samples.ribeye.data.Quantity
import ru.it_arch.tools.samples.ribeye.data.Resource
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@ConsistentCopyVisibility
@Serializable(with = SauceIngredientsImpl.Companion::class)
public data class SauceIngredientsImpl private constructor(
    override val macronutrients: Macronutrients,
    override val quantity: Quantity.Weight,
    override val expiration: Expiration
) : Resource.SauceIngredients {

    init {
        validate()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ValueObject.Data> fork(vararg args: Any?): T =
        Builder().apply {
            macronutrients = args[0] as Macronutrients
            quantity = args[1] as Quantity.Weight
            expiration = args[2] as Expiration
        }.build() as T

    public class Builder {
        public var macronutrients: Macronutrients? = null
        public var quantity: Quantity.Weight? = null
        public var expiration: Expiration? = null

        public fun build(): Resource.SauceIngredients {
            requireNotNull(macronutrients) { "SauceIngredients.macronutrients must be set" }
            requireNotNull(quantity) { "SauceIngredients.quantity must be set" }
            requireNotNull(expiration) { "SauceIngredients.expiration must be set" }

            return SauceIngredientsImpl(macronutrients!!, quantity!!, expiration!!)
        }
    }

    public class DslBuilder {
        public var macronutrients: Macronutrients? = null
        public var quantity: Long? = null
        public var expiration: Instant? = null

        public fun build(): Resource.SauceIngredients {
            requireNotNull(macronutrients) { "SauceIngredients.macronutrients must be set" }
            requireNotNull(quantity) { "SauceIngredients.quantity must be set" }
            requireNotNull(expiration) { "SauceIngredients.expiration must be set" }

            return SauceIngredientsImpl(
                macronutrients!!,
                QuantityWeightImpl(quantity!!),
                ExpirationImpl(expiration!!)
            )
        }
    }

    @OptIn(ExperimentalTime::class, ExperimentalSerializationApi::class)
    public companion object : KSerializer<SauceIngredientsImpl> {
        override val descriptor: SerialDescriptor =
            buildClassSerialDescriptor(SauceIngredientsImpl::class.java.name) {
                element<MacronutrientsImpl>("macronutrients")
                element<Long>("quantity")
                element<Instant>("expiration")
            }

        override fun serialize(encoder: Encoder, value: SauceIngredientsImpl) {
            encoder.encodeStructure(descriptor) {
                encodeSerializableElement(
                    descriptor,
                    0,
                    MacronutrientsImpl.serializer(),
                    value.macronutrients as MacronutrientsImpl
                )
                encodeLongElement(descriptor, 1, value.quantity.boxed)
                encodeSerializableElement(
                    descriptor,
                    2,
                    Instant.serializer(),
                    value.expiration.boxed
                )
            }
        }

        override fun deserialize(decoder: Decoder): SauceIngredientsImpl =
            decoder.decodeStructure(descriptor) {
                Builder().apply {
                    loop@ while (true) {
                        when (val i = decodeElementIndex(descriptor)) {
                            0 -> macronutrients = decodeSerializableElement(
                                descriptor,
                                0,
                                MacronutrientsImpl.serializer()
                            )
                            1 -> quantity =
                                QuantityWeightImpl(decodeLongElement(descriptor, 1))
                            2 -> expiration = decodeSerializableElement(
                                descriptor,
                                2,
                                Instant.serializer()
                            ).let { ExpirationImpl(it) }
                            DECODE_DONE -> break@loop
                            else -> throw SerializationException("Unexpected index $i")
                        }
                    }
                }.build() as SauceIngredientsImpl
            }
    }
}
