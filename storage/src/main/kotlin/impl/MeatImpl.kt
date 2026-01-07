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
@Serializable(with = MeatImpl.Companion::class)
public data class MeatImpl private constructor(
    override val macronutrients: Macronutrients,
    override val quantity: Quantity.Weight,
    override val expiration: Expiration
) : Resource.Meat {

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

        public fun build(): Resource.Meat {
            requireNotNull(macronutrients) { "Meat.macronutrients must be set" }
            requireNotNull(quantity) { "Meat.quantity must be set" }
            requireNotNull(expiration) { "Meat.expiration must be set" }

            return MeatImpl(macronutrients!!, quantity!!, expiration!!)
        }
    }

    public class DslBuilder {
        public var macronutrients: Macronutrients? = null
        public var quantity: Long? = null
        public var expiration: Instant? = null

        public fun build(): Resource.Meat {
            requireNotNull(macronutrients) { "Meat.macronutrients must be set" }
            requireNotNull(quantity) { "Meat.quantity must be set" }
            requireNotNull(expiration) { "Meat.expiration must be set" }

            return MeatImpl(
                macronutrients!!,
                QuantityWeightImpl(quantity!!),
                ExpirationImpl(expiration!!)
            )
        }
    }

    @OptIn(ExperimentalTime::class, ExperimentalSerializationApi::class)
    public companion object : KSerializer<MeatImpl> {
        override val descriptor: SerialDescriptor =
            buildClassSerialDescriptor(MeatImpl::class.java.name) {
                element<MacronutrientsImpl>("macronutrients")
                element<Long>("quantity")
                element<Instant>("expiration")
            }

        override fun serialize(encoder: Encoder, value: MeatImpl) {
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

        override fun deserialize(decoder: Decoder): MeatImpl =
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
                }.build() as MeatImpl
            }
    }
}
