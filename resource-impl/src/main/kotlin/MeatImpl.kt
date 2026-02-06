package ru.it_arch.tools.samples.ribeye.resource.impl

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
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
import ru.it_arch.tools.samples.ribeye.Expiration
import ru.it_arch.tools.samples.ribeye.Macronutrients
import ru.it_arch.tools.samples.ribeye.Quantity
import ru.it_arch.tools.samples.ribeye.Resource
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@ConsistentCopyVisibility
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
        /** Вес в граммах */
        public var quantity: Long? = null
        public var expiration: Instant? = null

        public fun build(): Resource.Meat {
            requireNotNull(macronutrients) { "Meat.macronutrients must be set" }
            requireNotNull(quantity) { "Meat.quantity must be set" }
            requireNotNull(expiration) { "Meat.expiration must be set" }

            return MeatImpl(
                macronutrients!!,
                Quantity.Weight(quantity!!),
                Expiration(expiration!!)
            )
        }
    }

    @OptIn(ExperimentalTime::class, ExperimentalSerializationApi::class)
    public companion object : KSerializer<MeatImpl> {
        override val descriptor: SerialDescriptor =
            buildClassSerialDescriptor(MeatImpl::class.java.name) {
                element<Macronutrients>("macronutrients")
                element<Long>("quantity")
                element<Instant>("expiration")
            }

        override fun serialize(encoder: Encoder, value: MeatImpl) {
            encoder.encodeStructure(descriptor) {
                encodeSerializableElement(
                    descriptor,
                    0,
                    MacronutrientsSerializer(),
                    value.macronutrients
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
                                MacronutrientsSerializer()
                            )
                            1 -> quantity =
                                Quantity.Weight(decodeLongElement(descriptor, 1))
                            2 -> expiration = decodeSerializableElement(
                                descriptor,
                                2,
                                Instant.Companion.serializer()
                            ).let { Expiration(it) }
                            DECODE_DONE -> break@loop
                            else -> throw SerializationException("Unexpected index $i")
                        }
                    }
                }.build() as MeatImpl
            }
    }
}
