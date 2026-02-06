package ru.it_arch.tools.samples.ribeye.storage

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.longs.shouldBeInRange
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.provided.neg
import io.kotest.provided.pos
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import ru.it_arch.tools.samples.ribeye.WriteResourceRepository
import ru.it_arch.tools.samples.ribeye.Quantity
import ru.it_arch.tools.samples.ribeye.Resource
import ru.it_arch.tools.samples.ribeye.dsl.grill
import ru.it_arch.tools.samples.ribeye.dsl.meat
import ru.it_arch.tools.samples.ribeye.dsl.rosemary
import ru.it_arch.tools.samples.ribeye.dsl.sauceIngredients
import ru.it_arch.tools.samples.ribeye.storage.impl.slotFactory
import ru.it_arch.tools.samples.ribeye.storage.slot.Slot
import kotlin.reflect.KClass
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class StorageTest: FunSpec({

    val emptyStorage: WriteResourceRepository = slotFactory(
        0.piece,
        0.weight,
        0.weight,
        0.piece
    ).let(::Storage)

    neg("Expired resource must not be added") {
        val rottenSauce = sauceIngredients {
            macronutrients = MacronutrientsImpl.DEFAULT
            quantity = 10
            expiration = Clock.System.now() - 10.days
        }

        val storage = slotFactory(
            0.piece,
            0.weight,
            100.weight,
            0.piece
        ).let(::Storage)

        storage.put(rottenSauce).shouldBeFailure<StorageError.RottenResource>()
    }

    context("Grill slot") {
        pos("Empty slot must return size 0") {
            emptyStorage.size<Resource.Grill, Quantity.Weight>() shouldBe 0.weight
        }

        neg("Empty slot.get() must return notFound result") {
            emptyStorage.pull<Resource.Grill>(10.weight)
                .shouldBeFailure<StorageError.NotFound>()
        }

        pos("must successfully put(), pull() and exact size 10") {
            val grillCapacity = 100.weight
            val requestQuantity = 90.weight

            val storage = slotFactory(
                0.piece,
                grillCapacity,
                0.weight,
                0.piece
            ).let(::Storage)

            val grill = grill {
                macronutrients = MacronutrientsImpl.DEFAULT
                quantity = requestQuantity.boxed
                expiration = Instant.DISTANT_FUTURE
            }
            storage.put(grill) shouldBeSuccess Unit
            storage.pull<Resource.Grill>(requestQuantity) shouldBeSuccess grill
            storage.size<Resource.Grill, Quantity.Weight>() shouldBe (grillCapacity - grill.quantity)
        }

        /* Условие теста:
        1. Заполняем Storage ресурсом. Теперь размер слота > 0.
        2. Вызываем Storage.size() для получения размера слота для этого ресурса.
        3. В процессе получения размера слота происходит коварная смена его состояния — кто-то
           в это время успел этот слот убить согласно логике Storage, и в этом случае Storage
           должен вернуть нулевой размер слота, не смотря на то, что фактически размер не нулевой. */
        neg("Killed slot must return size 0") {
            val testDispatcher = StandardTestDispatcher()
            val grillSlot = Slot.Weight(
                MacronutrientsImpl.DEFAULT.format(),
                Instant.DISTANT_FUTURE,
                100L,
                // необходимо подменить, чтобы во время паузы корутины слота иметь возможность
                // коварно убить этот слот и тем самым выполнить условие теста
                testDispatcher
            )
            val mockedSlotFactory = mockk<SlotFactory>()
            every { mockedSlotFactory.slotForGrill(any(), any()) } returns grillSlot
            val slots: Map<KClass<out Resource>, Slot> = mapOf(Resource.Grill::class to grillSlot)
            val storage: WriteResourceRepository = Storage(mockedSlotFactory, slots)

            // Эмуляция конкурентного доступа к слоту
            runTest(testDispatcher) {
                // Используем SupervisorJob и async/await вместо launch, чтобы ожидаемое исключение
                // IllegalStateException не порушило тест сразу — из-за structured concurrency
                val deferredStorageSizeExecution = async(SupervisorJob()) {
                    // Операция со слотом, которому суждено быть убитым:
                    // storage.size() вызовет Slot.size() убитого слота
                    storage.size<Resource.Grill, Quantity.Weight>()
                }

                // Делаем бяку: изменяем состояние слота путем добавления нового ресурса, что должно
                // убить текущий слот и создать новый. В это время корутина в Slot.size() { withContext { ... } }
                // на паузе в очереди у StandardTestDispatcher. Потом ловим исключение при
                // выполнении операции, как и запланировано логикой теста.
                grill {
                    macronutrients = MacronutrientsImpl.DEFAULT
                    quantity = 100
                    expiration = Instant.DISTANT_FUTURE
                }.also { storage.put(it) }


                // Дергаем testDispatcher на выполнение корутины `withContext` в Slot.size()
                // Она выкинет исключение при проверке своего состояния: второй ckeck(isActive) в Slot.Weight
                advanceUntilIdle()

                // Выполняем операцию чтения со слотом и получаем 0 вместо 100
                deferredStorageSizeExecution.await() shouldBe 0.weight
            }
        }
    }

    context("Sauce slot") {
        pos("Empty slot must return size 0") {
            emptyStorage.size<Resource.SauceIngredients, Quantity.Weight>() shouldBe 0.weight
        }

        neg("Empty slot.get() must return notFound result") {
            emptyStorage.pull<Resource.SauceIngredients>(10.weight)
                .shouldBeFailure<StorageError.NotFound>()
        }

        pos("must successfully put(), pull() and exact size 10") {
            val sauceCapacity = 100.weight
            val requestQuantity = 90.weight

            val storage = slotFactory(
                0.piece,
                0.weight,
                sauceCapacity,
                0.piece
            ).let(::Storage)

            val sauce = sauceIngredients {
                macronutrients = MacronutrientsImpl.DEFAULT
                quantity = requestQuantity.boxed
                expiration = Instant.DISTANT_FUTURE
            }
            storage.put(sauce) shouldBeSuccess Unit
            storage.pull<Resource.SauceIngredients>(requestQuantity) shouldBeSuccess sauce
            storage.size<Resource.SauceIngredients, Quantity.Weight>() shouldBe (sauceCapacity - sauce.quantity)
        }

        // С.м. Grill test
        neg("Killed slot must return size 0") {
            val testDispatcher = StandardTestDispatcher()
            val sauceSlot = Slot.Weight(
                MacronutrientsImpl.DEFAULT.format(),
                Instant.DISTANT_FUTURE,
                100L,
                testDispatcher
            )
            val mockedSlotFactory = mockk<SlotFactory>()
            every { mockedSlotFactory.slotForSauce(any(), any()) } returns sauceSlot
            val slots: Map<KClass<out Resource>, Slot> = mapOf(Resource.SauceIngredients::class to sauceSlot)
            val storage: WriteResourceRepository = Storage(mockedSlotFactory, slots)

            runTest(testDispatcher) {
                val deferredStorageSizeExecution = async(SupervisorJob()) {
                    storage.size<Resource.SauceIngredients, Quantity.Weight>()
                }

                sauceIngredients {
                    macronutrients = MacronutrientsImpl.DEFAULT
                    quantity = 100
                    expiration = Instant.DISTANT_FUTURE
                }.also { storage.put(it) }

                advanceUntilIdle()
                deferredStorageSizeExecution.await() shouldBe 0.weight
            }
        }
    }

    context("Rosemary slot") {
        pos("Empty slot must return size 0") {
            emptyStorage.size<Resource.Rosemary, Quantity.Piece>() shouldBe 0.piece
        }

        neg("Empty slot.get() must return notFound result") {
            emptyStorage.pull<Resource.Rosemary>(10.piece)
                .shouldBeFailure<StorageError.NotFound>()
        }

        pos("must successfully put(), pull() and exact size 10") {
            val rosemaryCapacity = 100.piece
            val requestQuantity = 90.piece

            val storage = slotFactory(
                0.piece,
                0.weight,
                0.weight,
                rosemaryCapacity
            ).let(::Storage)

            val rosemary = rosemary {
                macronutrients = MacronutrientsImpl.DEFAULT
                quantity = requestQuantity.boxed
                expiration = Instant.DISTANT_FUTURE
            }
            storage.put(rosemary) shouldBeSuccess Unit
            storage.pull<Resource.Rosemary>(requestQuantity) shouldBeSuccess rosemary
            storage.size<Resource.Rosemary, Quantity.Weight>() shouldBe (rosemaryCapacity - rosemary.quantity)
        }

        // С.м. Grill test
        neg("Killed slot must return size 0") {
            val testDispatcher = StandardTestDispatcher()
            val rosemarySlot = Slot.Weight(
                MacronutrientsImpl.DEFAULT.format(),
                Instant.DISTANT_FUTURE,
                100,
                testDispatcher
            )
            val mockedSlotFactory = mockk<SlotFactory>()
            every { mockedSlotFactory.slotForRosemary(any(), any()) } returns rosemarySlot
            val slots: Map<KClass<out Resource>, Slot> = mapOf(Resource.Rosemary::class to rosemarySlot)
            val storage: WriteResourceRepository = Storage(mockedSlotFactory, slots)

            runTest(testDispatcher) {
                val deferredStorageSizeExecution = async(SupervisorJob()) {
                    storage.size<Resource.Rosemary, Quantity.Piece>()
                }

                rosemary {
                    macronutrients = MacronutrientsImpl.DEFAULT
                    quantity = 100
                    expiration = Instant.DISTANT_FUTURE
                }.also { storage.put(it) }

                advanceUntilIdle()
                deferredStorageSizeExecution.await() shouldBe 0.piece
            }
        }
    }

    context("Meat slot") {
        val meatStub = meat {
            macronutrients = macronutrients {
                proteins = 20.0
                fats = 19.0
                carbs = 0.0
                calories = 260.0
            }
            quantity = 350
            expiration = Clock.System.now() + 48.hours
        }
        val meatSuccess = meat {
            macronutrients = macronutrients {
                proteins = 20.0
                fats = 19.0
                carbs = 0.0
                calories = 260.0
            }
            quantity = 300
            expiration = Clock.System.now() + 48.hours
        }

        pos("Empty slot must return size 0") {
            emptyStorage.size<Resource.Meat, Quantity.Piece>() shouldBe 0.piece
        }

        neg("Empty slot.get() must return notFound result") {
            emptyStorage.pull<Resource.Meat>(10.weight)
                .shouldBeFailure<StorageError.NotFound>()
        }

        pos("pull() should apply exponential backoff on retries") {
            val meatSlotStubVersion = 10
            val mockedMeatSlot = mockk<Slot.Reusable>()
            every { mockedMeatSlot.currentVersion } returns meatSlotStubVersion
            coEvery { mockedMeatSlot.add(any()) } returns Result.success(meatSlotStubVersion + 1)

            // Mock Scenario:
            // 1. First call: Returns version 12 (Drift detected: 12 != 10 + 1) -> Retry
            // 2. Second call: Returns version 14 (Drift detected: 14 != 10 + 1) -> Retry
            // 3. Third call: Returns version 11 (Success: 11 == 10 + 1) -> Success
            coEvery { mockedMeatSlot.pull(any()) } returns
                    Result.success(12 to meatStub.format()) andThen
                    Result.success(14 to meatStub.format()) andThen
                    Result.success((meatSlotStubVersion + 1) to meatSuccess.format())

            val mockedSlotFactory = mockk<SlotFactory>()
            every { mockedSlotFactory.slotForMeat() } returns mockedMeatSlot
            val slots: Map<KClass<out Resource>, Slot> = mapOf(Resource.Meat::class to mockedMeatSlot)

            val storage: WriteResourceRepository = Storage(mockedSlotFactory, slots)

            runTest {
                val startTime = currentTime
                val result = storage.pull<Resource.Meat>(QuantityWeightImpl(100))

                result.isSuccess shouldBe true
                result.getOrThrow() shouldBe meatSuccess

                // Проверка задержки. Время виртуальное:
                // первая попытка с выбросом исключения -> delay(Storage.BASE_DELAY)
                // вторая попытка с выбросом исключения -> delay(Storage.BASE_DELAY * Storage.EXP)
                // Общее время: delay(BASE_DELAY * Storage.EXP + BASE_DELAY)

                val jitter = ((Storage.BASE_DELAY * Storage.EXP + Storage.BASE_DELAY) * Storage.JITTER_FACTOR).toLong()

                val elapsed = currentTime - startTime
                println("elapsed range: [${elapsed - jitter}, ${elapsed + jitter}] ms, jitter: $jitter")
                elapsed shouldBeInRange (elapsed - jitter)..(elapsed + jitter)
            }
        }
    }
})
