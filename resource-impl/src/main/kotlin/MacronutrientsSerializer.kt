package ru.it_arch.tools.samples.ribeye.resource.impl

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import ru.it_arch.tools.samples.ribeye.Macronutrients

internal class MacronutrientsSerializer : KSerializer<Macronutrients> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor(Macronutrients::class.java.name) {
            element<Double>("proteins")
            element<Double>("fats")
            element<Double>("carbs")
            element<Double>("calories")
        }

    override fun serialize(encoder: Encoder, value: Macronutrients) {
        encoder.encodeStructure(descriptor) {
            encodeDoubleElement(descriptor, 0, value.proteins.boxed)
            encodeDoubleElement(descriptor, 1, value.fats.boxed)
            encodeDoubleElement(descriptor, 2, value.carbs.boxed)
            encodeDoubleElement(descriptor, 3, value.calories.boxed)
        }
    }

    override fun deserialize(decoder: Decoder): Macronutrients =
        decoder.decodeStructure(descriptor) {
            Macronutrients.Builder().apply {
                loop@ while (true) {
                    when (val i = decodeElementIndex(descriptor)) {
                        0 -> proteins = decodeDoubleElement(descriptor, 0)
                            .let { Macronutrients.Proteins(it) }
                        1 -> fats = decodeDoubleElement(descriptor, 1)
                            .let { Macronutrients.Fats(it) }
                        2 -> carbs = decodeDoubleElement(descriptor, 2)
                            .let { Macronutrients.Carbohydrates(it) }
                        3 -> calories = decodeDoubleElement(descriptor, 3)
                            .let { Macronutrients.Kcal(it) }
                        DECODE_DONE -> break@loop
                        else -> throw SerializationException("Unexpected index $i")
                    }
                }
            }.build()
        }
}
