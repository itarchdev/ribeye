package ru.it_arch.tools.samples.ribeye.dsl.impl

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import ru.it_arch.k3dm.ValueObject
import ru.it_arch.tools.samples.ribeye.dsl.Macronutrients

@ConsistentCopyVisibility
@Serializable(with = MacronutrientsImpl.Companion::class)
public data class MacronutrientsImpl private constructor(
    override val proteins: Macronutrients.Proteins,
    override val fats: Macronutrients.Fats,
    override val carbs: Macronutrients.Carbohydrates,
    override val calories: Macronutrients.Kcal
) : Macronutrients {

    init {
        validate()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ValueObject.Data> fork(vararg args: Any?): T =
        Builder().apply {
            proteins = args[0] as Macronutrients.Proteins
            fats = args[1] as Macronutrients.Fats
            carbs = args[2] as Macronutrients.Carbohydrates
            calories = args[3] as Macronutrients.Kcal
        }.build() as T

    public class Builder {
        public var proteins: Macronutrients.Proteins? = null
        public var fats: Macronutrients.Fats? = null
        public var carbs: Macronutrients.Carbohydrates? = null
        public var calories: Macronutrients.Kcal? = null

        public fun build(): Macronutrients {
            requireNotNull(proteins) { "proteins must be set" }
            requireNotNull(fats) { "fats must be set" }
            requireNotNull(carbs) { "carbs must be set" }
            requireNotNull(calories) { "calories must be set" }

            return MacronutrientsImpl(proteins!!, fats!!, carbs!!, calories!!)
        }
    }

    public class DslBuilder {
        public var proteins: Double? = null
        public var fats: Double? = null
        public var carbs: Double? = null
        public var calories: Double? = null

        public fun build(): Macronutrients {
            requireNotNull(proteins) { "proteins must be set" }
            requireNotNull(fats) { "fats must be set" }
            requireNotNull(carbs) { "carbs must be set" }
            requireNotNull(calories) { "calories must be set" }

            return MacronutrientsImpl(
                ProteinsImpl(proteins!!),
                FatsImpl(fats!!),
                CarbohydratesImpl(carbs!!),
                KcalImpl(calories!!)
            )
        }
    }

    @JvmInline
    public value class ProteinsImpl private constructor(
        override val boxed: Double
    ) : Macronutrients.Proteins {
        init {
            validate()
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T : ValueObject.Value<Double>> apply(boxed: Double): T =
            ProteinsImpl(boxed) as T

        override fun toString(): String =
            "%.1f".format(boxed)

        public companion object {
            public val DEFAULT: Macronutrients.Proteins = ProteinsImpl(0.0)
            public operator fun invoke(value: Double): Macronutrients.Proteins =
                ProteinsImpl(value)
        }
    }

    @JvmInline
    public value class FatsImpl private constructor(
        override val boxed: Double
    ) : Macronutrients.Fats {
        init {
            validate()
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T : ValueObject.Value<Double>> apply(boxed: Double): T =
            FatsImpl(boxed) as T

        override fun toString(): String =
            "%.1f".format(boxed)

        public companion object {
            public val DEFAULT: Macronutrients.Fats = FatsImpl(0.0)

            public operator fun invoke(value: Double): Macronutrients.Fats =
                FatsImpl(value)
        }
    }

    @JvmInline
    public value class CarbohydratesImpl private constructor(
        override val boxed: Double
    ) : Macronutrients.Carbohydrates {
        init {
            validate()
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T : ValueObject.Value<Double>> apply(boxed: Double): T =
            CarbohydratesImpl(boxed) as T

        override fun toString(): String =
            "%.1f".format(boxed)

        public companion object {
            public val DEFAULT: Macronutrients.Carbohydrates =
                CarbohydratesImpl(0.0)

            public operator fun invoke(value: Double): Macronutrients.Carbohydrates =
                CarbohydratesImpl(value)
        }
    }

    @JvmInline
    public value class KcalImpl private constructor(
        override val boxed: Double
    ) : Macronutrients.Kcal {
        init {
            validate()
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T : ValueObject.Value<Double>> apply(boxed: Double): T =
            KcalImpl(boxed) as T

        override fun toString(): String =
            "%.1f".format(boxed)

        public companion object {
            public val DEFAULT: Macronutrients.Kcal = KcalImpl(0.0)

            public operator fun invoke(value: Double): Macronutrients.Kcal =
                KcalImpl(value)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    public companion object : KSerializer<MacronutrientsImpl> {
        public val DEFAULT: Macronutrients = MacronutrientsImpl(
            ProteinsImpl.DEFAULT,
            FatsImpl.DEFAULT,
            CarbohydratesImpl.DEFAULT,
            KcalImpl.DEFAULT
        )

        override val descriptor: SerialDescriptor =
            buildClassSerialDescriptor(MacronutrientsImpl::class.java.name) {
                element<Double>("proteins")
                element<Double>("fats")
                element<Double>("carbs")
                element<Double>("calories")
            }

        override fun serialize(encoder: Encoder, value: MacronutrientsImpl) {
            encoder.encodeStructure(descriptor) {
                encodeDoubleElement(descriptor, 0, value.proteins.boxed)
                encodeDoubleElement(descriptor, 1, value.fats.boxed)
                encodeDoubleElement(descriptor, 2, value.carbs.boxed)
                encodeDoubleElement(descriptor, 3, value.calories.boxed)
            }
        }

        override fun deserialize(decoder: Decoder): MacronutrientsImpl =
            decoder.decodeStructure(descriptor) {
                Builder().apply {
                    loop@ while (true) {
                        when (val i = decodeElementIndex(descriptor)) {
                            0 -> proteins = decodeDoubleElement(descriptor, 0)
                                .let { ProteinsImpl(it) }
                            1 -> fats = decodeDoubleElement(descriptor, 1)
                                .let { FatsImpl(it) }
                            2 -> carbs = decodeDoubleElement(descriptor, 2)
                                .let { CarbohydratesImpl(it) }
                            3 -> calories = decodeDoubleElement(descriptor, 3)
                                .let { KcalImpl(it) }
                            DECODE_DONE -> break@loop
                            else -> throw SerializationException("Unexpected index $i")
                        }
                    }
                }.build() as MacronutrientsImpl
            }
    }
}
