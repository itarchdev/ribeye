package ru.it_arch.tools.samples.ribeye.dsl.impl

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
import ru.it_arch.tools.samples.ribeye.dsl.Expiration
import ru.it_arch.tools.samples.ribeye.dsl.Macronutrients
import ru.it_arch.tools.samples.ribeye.dsl.Quantity
import ru.it_arch.tools.samples.ribeye.dsl.Resource
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@ConsistentCopyVisibility
@Serializable(with = RosemaryImpl.Companion::class)
public data class RosemaryImpl private constructor(
    override val macronutrients: Macronutrients,
    override val quantity: Quantity.Piece,
    override val expiration: Expiration
) : Resource.Rosemary {

    init {
        validate()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ValueObject.Data> fork(vararg args: Any?): T =
        Builder().apply {
            macronutrients = args[0] as Macronutrients
            quantity = args[1] as Quantity.Piece
            expiration = args[2] as Expiration
        }.build() as T

    public class Builder {
        public var macronutrients: Macronutrients? = null
        public var quantity: Quantity.Piece? = null
        public var expiration: Expiration? = null

        public fun build(): Resource.Rosemary {
            requireNotNull(macronutrients) { "Rosemary.macronutrients must be set" }
            requireNotNull(quantity) { "Rosemary.quantity must be set" }
            requireNotNull(expiration) { "Rosemary.expiration must be set" }

            return RosemaryImpl(macronutrients!!, quantity!!, expiration!!)
        }
    }

    public class DslBuilder {
        public var macronutrients: Macronutrients? = null
        public var quantity: Int? = null
        public var expiration: Instant? = null

        public fun build(): Resource.Rosemary {
            requireNotNull(macronutrients) { "Rosemary.macronutrients must be set" }
            requireNotNull(quantity) { "Rosemary.quantity must be set" }
            requireNotNull(expiration) { "Rosemary.expiration must be set" }

            return RosemaryImpl(
                macronutrients!!,
                QuantityPieceImpl(quantity!!),
                ExpirationImpl(expiration!!)
            )
        }
    }

    @OptIn(ExperimentalTime::class, ExperimentalSerializationApi::class)
    public companion object : KSerializer<RosemaryImpl> {
        override val descriptor: SerialDescriptor =
            buildClassSerialDescriptor(RosemaryImpl::class.java.name) {
                element<MacronutrientsImpl>("macronutrients")
                element<Int>("quantity")
                element<Instant>("expiration")
            }

        override fun serialize(encoder: Encoder, value: RosemaryImpl) {
            encoder.encodeStructure(descriptor) {
                encodeSerializableElement(
                    descriptor,
                    0,
                    MacronutrientsImpl.Companion.serializer(),
                    value.macronutrients as MacronutrientsImpl
                )
                encodeIntElement(descriptor, 1, value.quantity.boxed)
                encodeSerializableElement(
                    descriptor,
                    2,
                    Instant.Companion.serializer(),
                    value.expiration.boxed
                )
            }
        }

        override fun deserialize(decoder: Decoder): RosemaryImpl =
            decoder.decodeStructure(descriptor) {
                Builder().apply {
                    loop@ while (true) {
                        when (val i = decodeElementIndex(descriptor)) {
                            0 -> macronutrients = decodeSerializableElement(
                                descriptor,
                                0,
                                MacronutrientsImpl.Companion.serializer()
                            )
                            1 -> quantity =
                                QuantityPieceImpl(decodeIntElement(descriptor, 1))
                            2 -> expiration = decodeSerializableElement(
                                descriptor,
                                2,
                                Instant.Companion.serializer()
                            ).let { ExpirationImpl(it) }
                            DECODE_DONE -> break@loop
                            else -> throw SerializationException("Unexpected index $i")
                        }
                    }
                }.build() as RosemaryImpl
            }
    }
}
