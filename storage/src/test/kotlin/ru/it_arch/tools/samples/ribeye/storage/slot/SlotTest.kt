package ru.it_arch.tools.samples.ribeye.storage.slot

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldNotBeLessThan
import io.kotest.matchers.longs.shouldBeInRange
import io.kotest.matchers.shouldBe
import io.kotest.provided.neg
import io.kotest.provided.pos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import ru.it_arch.tools.samples.ribeye.storage.impl.QuantityWeightImpl
import ru.it_arch.tools.samples.ribeye.storage.impl.format
import ru.it_arch.tools.samples.ribeye.storage.impl.macronutrients
import ru.it_arch.tools.samples.ribeye.storage.impl.meat
import ru.it_arch.tools.samples.ribeye.storage.impl.rosemary
import ru.it_arch.tools.samples.ribeye.storage.impl.sauceIngredients
import ru.it_arch.tools.samples.ribeye.storage.impl.toDslBuilder
import ru.it_arch.tools.samples.ribeye.storage.impl.toMeat
import ru.it_arch.tools.samples.ribeye.storage.impl.toRosemary
import ru.it_arch.tools.samples.ribeye.storage.impl.toSauceIngredients
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalKotest::class, ExperimentalCoroutinesApi::class, InternalCoroutinesApi::class)
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
    val meat = meat {
        macronutrients = macronutrients {
            proteins = 20.0
            fats = 19.0
            carbs = 0.0
            calories = 260.0
        }
        quantity = 350
        expiration = Clock.System.now() + 48.hours
    }

    context("Slot.Piece") {
        val capacity = 100
        val iterations = 150
        val requestQuantity = 1

        val slot = Slot.Piece(
            rosemary.macronutrients.format(),
            rosemary.expiration.boxed,
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

        /* Стратегия тестирования двойной проверки активного состояния слота.
        1. Первая проверка `check(isActive)`: вызов `slot.get()` и старт `withContext` в StandardTestDispatcher.
        2. Окно между помещением корутины в очередь StandardTestDispatcher, паузой и ее выполнением.
        3. Убиваем слот вызовом `slot.kill()`: isActive = false.
        4. Запуск помещенной в очередь корутины: дергаем StandardTestDispatcher на продолжение выполнения.
        5. Вторая проверка `check(isActive)`: теперь должна выкинуть исключение. */
        pos("Double-checking must catch IllegalStateException during the dispatch gap") {
            // StandardTestDispatcher для ручного контроля выполнения
            val testDispatcher = StandardTestDispatcher()
            val slot = Slot.Piece(
                rosemary.macronutrients.format(),
                rosemary.expiration.boxed,
                capacity,
                testDispatcher
            )
            runTest(testDispatcher) {
                // Используем SupervisorJob и async/await, чтобы ожидаемое исключение
                // IllegalStateException не порушило тест сразу — в соответствии со structured concurrency
                val deferred = async(SupervisorJob()) {
                    slot.get("1") // 1. Первый вызов `check(isActive)`
                }
                // 2. Окно возможности: корутина `withContext` ждет в очереди на паузе.
                // Сбрасываем флаг активности до того, как testDispatcher начнет выполнять корутину
                slot.kill() // 3. isActive = false
                advanceUntilIdle() // 4. Дергаем testDispatcher на выполнение корутины `withContext`

                shouldThrow<IllegalStateException> {
                    deferred.await() // 5. На втором вызове `check(isActive)` выкинется исключение, т.к. isActive уже false
                }
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
            sauceIngredients.expiration.boxed,
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

        /* Стратегия тестирования двойной проверки активного состояния слота.
        1. Первая проверка `check(isActive)`: вызов `slot.get()` и старт `withContext` в StandardTestDispatcher.
        2. Окно между помещением корутины в очередь StandardTestDispatcher, паузой и ее выполнением.
        3. Убиваем слот вызовом `slot.kill()`: isActive = false.
        4. Запуск помещенной в очередь корутины: дергаем StandardTestDispatcher на продолжение выполнения.
        5. Вторая проверка `check(isActive)`: теперь должна выкинуть исключение. */
        pos("Double-checking must catch IllegalStateException during the dispatch gap") {
            // StandardTestDispatcher для ручного контроля выполнения
            val testDispatcher = StandardTestDispatcher()
            val slot = Slot.Weight(
                sauceIngredients.macronutrients.format(),
                sauceIngredients.expiration.boxed,
                capacity,
                testDispatcher
            )
            runTest(testDispatcher) {
                // Используем SupervisorJob и async/await, чтобы ожидаемое исключение
                // IllegalStateException не порушило тест сразу — в соответствии со structured concurrency
                val deferred = async(SupervisorJob()) {
                    slot.get("300") // 1. Первый вызов `check(isActive)`
                }
                // 2. Окно возможности: корутина `withContext` ждет в очереди на паузе.
                // Сбрасываем флаг активности до того, как testDispatcher начнет выполнять корутину
                slot.kill() // 3. isActive = false
                advanceUntilIdle() // 4. Дергаем testDispatcher на выполнение корутины `withContext`

                shouldThrow<IllegalStateException> {
                    deferred.await() // 5. На втором вызове `check(isActive)` выкинется исключение, т.к. isActive уже false
                }
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
        context("Testing concurrency") {
            pos("Successful adds must be equal successful gets + remain size") {
                val iterations = 200
                val slot = Slot.Pack(50)

                val successfulAdds: Int
                val successfulGets: Int

                coroutineScope {
                    val producers = (1..iterations).map {
                        async(Dispatchers.Default) { slot.add(meat.format()) }
                    }
                    val consumers = (1..iterations).map {
                        async(Dispatchers.Default) {
                            slot.get("100")
                        }
                    }
                    successfulAdds = producers.awaitAll().count { it.isSuccess }
                    successfulGets = consumers.awaitAll().count { it.isSuccess }
                }

                successfulAdds shouldBe (successfulGets + slot.size())
            }

            pos("Only ONE consumer should have succeeded, the rest must fail") {
                val slot = Slot.Pack(1).apply { add(meat.format()) }

                val results = coroutineScope {
                    (1..100).map {
                        async(Dispatchers.Default) { slot.get("100") }
                    }.awaitAll()
                }

                results.count { it.isSuccess } shouldBe 1
                slot.size() shouldBe 0
            }
        }

        pos("Meat, requested by quantity 350, must be not less than 350") {
            val slot = Slot.Pack(3).apply {
                meat.toDslBuilder().apply { quantity = 300 }.build().format().also { add(it) }
                meat.toDslBuilder().apply { quantity = 400 }.build().format().also { add(it) }
                meat.toDslBuilder().apply { quantity = 320 }.build().format().also { add(it) }
            }
            slot.get("350").getOrThrow().second.toMeat().quantity shouldNotBeLessThan QuantityWeightImpl.Companion(
                350
            )
        }
    }
})
