package ru.it_arch.tools.samples.ribeye.storage

import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.longs.shouldBeInRange
import io.kotest.matchers.shouldBe
import io.kotest.provided.neg
import io.kotest.provided.pos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import ru.it_arch.tools.samples.ribeye.storage.impl.format
import ru.it_arch.tools.samples.ribeye.storage.impl.macronutrients
import ru.it_arch.tools.samples.ribeye.storage.impl.rosemary
import ru.it_arch.tools.samples.ribeye.storage.impl.sauceIngredients
import ru.it_arch.tools.samples.ribeye.storage.impl.toDslBuilder
import ru.it_arch.tools.samples.ribeye.storage.impl.toRosemary
import ru.it_arch.tools.samples.ribeye.storage.impl.toSauceIngredients
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

@OptIn(ExperimentalKotest::class)
class SlotTest : FunSpec({

    val rosemary = rosemary {
        macronutrients = macronutrients {
            proteins = 3.3
            fats = 5.9
            carbs = 20.7
            calories = 131.0
        }
        quantity = 1
        expiration = Clock.System.now() + 300.days
    }
    val sauceIngredients = sauceIngredients {
        macronutrients = macronutrients {
            proteins = 8.0
            fats = 0.5
            carbs = 7.5
            calories = 65.0
        }
        quantity = 1
        expiration = Clock.System.now() + 100.days
    }

    context("Slot.Piece") {
        val capacity = 100
        val iterations = 150
        val requestQuantity = 1

        val slot = Slot.Piece(
            rosemary.macronutrients.format(),
            rosemary.expiration.boxed.toString(),
            capacity
        )
        // Тестирование конкурентного доступа: запуск на множестве корутин, исчерпыващих максимальный
        // размер слота с целью собрать ожидаемые успешные и неуспешные результаты
        val results = coroutineScope {
            (1..iterations).map {
                async(Dispatchers.Default) {
                    slot.get("$requestQuantity")
                }
            }.awaitAll()
        }

        context("Testing concurrency") {
            pos("Total successful `get()` must equal the capacity: $capacity") {
                results.count { it.isSuccess } shouldBe capacity
            }

            neg("Requests exceeding capacity: ${iterations - capacity} must fail") {
                results.count { it.isFailure } shouldBe (iterations - capacity)
            }

            pos("Remain slot size must be exactly 0") {
                slot.size() shouldBe 0
            }
        }

        pos("Slot must return JSON representation of expected Rosemary object") {
            // Достаточно проверить первый элемент выдачи и удедиться, что его можно десериализовать
            rosemary.toDslBuilder().apply { quantity = requestQuantity }.build().also { expected ->
                results.first { it.isSuccess }.getOrNull()!!.toRosemary() shouldBe expected
            }
        }
    }

    context("Slot.Weight") {
        val capacity = 10_000L
        val iterations = 150
        val requestQuantity = 300L

        val slot = Slot.Weight(
            sauceIngredients.macronutrients.format(),
            sauceIngredients.expiration.boxed.toString(),
            capacity
        )
        // Тестирование конкурентного доступа: запуск на множестве корутин, исчерпыващих
        // вместимость слота с целью собрать ожидаемые успешные и неуспешные результаты
        val results = coroutineScope {
            (1..iterations).map {
                async(Dispatchers.Default) {
                    slot.get("$requestQuantity")
                }
            }.awaitAll()
        }

        context("Testing concurrency") {
            val canGet = (capacity / requestQuantity).toInt()
            pos("Total successful `get()` must <= slot can get: $canGet") {
                results.count { it.isSuccess } shouldBe canGet
            }

            neg("Requests exceeding capacity: ${iterations - canGet} must fail") {
                results.count { it.isFailure } shouldBe (iterations - canGet)
            }

            pos("Remain slot size ${slot.size()} must be >= 0 and < $requestQuantity") {
                slot.size() shouldBeInRange 0..<requestQuantity
            }
        }

        pos("Slot must return JSON representation of expected SauceIngredients object") {
            // Достаточно проверить первый элемент выдачи и удедиться, что его можно десериализовать
            sauceIngredients.toDslBuilder().apply { quantity = requestQuantity }.build().also { expected ->
                results.first { it.isSuccess }.getOrNull()!!.toSauceIngredients() shouldBe expected
            }
        }
    }

    context("Slot.Pack") {
        val slot = Slot.Pack(10)

    }
})
