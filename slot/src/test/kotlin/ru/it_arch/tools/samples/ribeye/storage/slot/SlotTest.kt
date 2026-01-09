package ru.it_arch.tools.samples.ribeye.storage.slot

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldNotBeLessThan
import io.kotest.matchers.longs.shouldBeInRange
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.provided.neg
import io.kotest.provided.pos
import io.kotest.provided.tech
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import ru.it_arch.tools.samples.ribeye.data.Resource
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

@OptIn(
    ExperimentalKotest::class,
    ExperimentalCoroutinesApi::class,
    InternalCoroutinesApi::class
)
class SlotTest : FunSpec({
    val rosemary = mockk<Resource.Rosemary>()
    every { rosemary.macronutrients.proteins.boxed } returns 3.3
    every { rosemary.macronutrients.fats.boxed } returns 5.9
    every { rosemary.macronutrients.carbs.boxed } returns 20.7
    every { rosemary.macronutrients.calories.boxed } returns 131.0
    every { rosemary.quantity.boxed } returns 1
    every { rosemary.expiration.boxed } returns Clock.System.now() + 300.days

    val sauceIngredients = mockk<Resource.SauceIngredients>()
    every { sauceIngredients.macronutrients.proteins.boxed } returns 8.0
    every { sauceIngredients.macronutrients.fats.boxed } returns 0.5
    every { sauceIngredients.macronutrients.carbs.boxed } returns 7.5
    every { sauceIngredients.macronutrients.calories.boxed } returns 65.0
    every { sauceIngredients.quantity.boxed } returns 1
    every { sauceIngredients.expiration.boxed } returns Clock.System.now() + 100.days

    val meat = mockk<Resource.Meat>()
    every { meat.macronutrients.proteins.boxed } returns 20.0
    every { meat.macronutrients.fats.boxed } returns 19.0
    every { meat.macronutrients.carbs.boxed } returns 0.0
    every { meat.macronutrients.calories.boxed } returns 260.0
    every { meat.quantity.boxed } returns 350
    every { meat.expiration.boxed } returns Clock.System.now() + 48.hours

    /*
    tech("Simple test for test this Unit-test") {
        Slot.Piece(
            rosemary.macronutrients.format(),
            rosemary.expiration.boxed,
            1
        ).get("2").shouldBeFailure()
    }*/

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
            results.first { it.isSuccess }.getOrThrow() shouldBe Slot.buildResponse(
                rosemary.macronutrients.format(),
                requestQuantity.toString(),
                rosemary.expiration.boxed
            )
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
            results.first { it.isSuccess }.getOrThrow() shouldBe Slot.buildResponse(
                sauceIngredients.macronutrients.format(),
                requestQuantity.toString(),
                sauceIngredients.expiration.boxed
            )
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

            pos("`get()` must increment version and return correct snapshot") {
                Slot.Pack(1).apply { add(meat.format()) }.also { slot ->
                    val versionStart = slot.currentVersion // version = 1 after add()
                    slot.get("100").getOrThrow().also { (version, meatStr) -> // version = 2 after get()
                        version shouldBe (versionStart + 1)
                        meatStr shouldBe meat.format()
                    }
                    slot.currentVersion shouldBe 2
                }
            }

            pos("Concurrent operations should maintain version integrity") {
                val jobsCount = 100
                val opsPerJob = 100
                val total = jobsCount * opsPerJob
                val content = meat.format()
                val slot = Slot.Pack(total)

                runTest {
                    withContext(Dispatchers.Default) {
                        coroutineScope {
                            repeat(jobsCount) {
                                launch {
                                    repeat(opsPerJob) { slot.add(content) }
                                }
                            }
                        }
                    }
                }
                slot.currentVersion shouldBe total
            }

            neg("Must detect concurrent modification during get") {
                val slot = Slot.Pack(2).apply { add(meat.format()) }
                runTest {
                    val startVersion = slot.currentVersion // version = 1

                    // В "процессе" версия должна измениться "неожиданным" вызовом `add`
                    slot.add(meat.format()) // version = 2

                    slot.get("50").getOrThrow().also { (version, _) ->
                        // "Ожидаем", что версия будет 2, но по факту 3
                        println("\"expecting\" version: ${startVersion + 1} result.version: $version")
                        (version == startVersion + 1) shouldBe false
                    }
                }
            }
        }

        pos("Meat, requested by quantity 350, must be not less than 350") {
            every { meat.quantity.boxed } returnsMany listOf(300, 400, 320)

            Slot.Pack(3).apply {
                add(meat.format())
                add(meat.format())
                add(meat.format())
            }.get("350").getOrThrow().second.fetchWeightQuantity() shouldNotBeLessThan 350
        }
    }
})
