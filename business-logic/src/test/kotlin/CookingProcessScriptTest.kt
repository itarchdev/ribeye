package ru.it_arch.tools.samples.ribeye.bl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.shouldBe
import io.kotest.provided.neg
import io.kotest.provided.pos
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import ru.it_arch.tools.samples.ribeye.ResourceRepository
import ru.it_arch.tools.samples.ribeye.CookingProcess
import ru.it_arch.tools.samples.ribeye.Op
import ru.it_arch.tools.samples.ribeye.State
import kotlin.coroutines.cancellation.CancellationException
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class CookingProcessScriptTest : FunSpec({

    fun <T : Op> defaultState(type: KClass<out T>): State<T> =
        StateImpl.Builder<T>().apply {
            opType = type
            macronutrients = mockk()
            quantity = mockk()
            elapsed = 1.days
            value = mockk()
        }.build()

    val opGrillGet = mockk<Op.Grill.Get>()
    coEvery { opGrillGet.invoke(any()) } returns
            Result.success(defaultState(Op.Grill.Get::class))
    val opGrillCheck = mockk<Op.Grill.Check>()
    coEvery { opGrillCheck.invoke(any()) } returns
            Result.success(defaultState(Op.Grill.Check::class))

    val opSauceGet = mockk<Op.Sauce.Get>()
    coEvery { opSauceGet.invoke(any()) } returns
            Result.success(defaultState(Op.Sauce.Get::class))
    val opSaucePrepare = mockk<Op.Sauce.Prepare>()
    coEvery { opSaucePrepare.invoke(any()) } returns
            Result.success(defaultState(Op.Sauce.Prepare::class))

    val opRosemaryGet = mockk<Op.Rosemary.Get>()
    coEvery { opRosemaryGet.invoke(any()) } returns
            Result.success(defaultState(Op.Rosemary.Get::class))
    val opRosemaryRoast = mockk<Op.Rosemary.Roast>()
    coEvery { opRosemaryRoast.invoke(any()) } returns
            Result.success(defaultState(Op.Rosemary.Roast::class))

    val opMeatGet = mockk<Op.Meat.Get>()
    coEvery { opMeatGet.invoke(any()) } returns
            Result.success(defaultState(Op.Meat.Get::class))
    val opMeatCheck = mockk<Op.Meat.Check>()
    coEvery { opMeatCheck.invoke(any()) } returns
            Result.success(defaultState(Op.Meat.Check::class))
    val opMeatMarinate = mockk<Op.Meat.Marinate>()
    coEvery { opMeatMarinate.invoke(any()) } returns
            Result.success(defaultState(Op.Meat.Marinate::class))
    val opMeatPrepare = mockk<Op.Meat.PrepareForRoasting>()
    coEvery { opMeatPrepare.invoke(any(), any()) } returns
            Result.success(defaultState(Op.Meat.PrepareForRoasting::class))
    val opMeatRoast = mockk<Op.Meat.Roast>()
    coEvery { opMeatRoast.invoke(any()) } returns
            Result.success(defaultState(Op.Meat.Roast::class))
    val opMeatServe = mockk<Op.Meat.Serve>()
    coEvery { opMeatServe.invoke(any(), any(), any()) } returns
            Result.success(defaultState(Op.Meat.Serve::class))

    val opFinish = mockk<Op.Finish>()
    coEvery { opFinish.invoke(any()) } returns
            Result.success(defaultState(Op.Finish::class))
            //Result.failure(RuntimeException())

    val cookingProcess = mockk<CookingProcess>()

    coEvery { cookingProcess.`get meat from storage` } returns opMeatGet
    coEvery { cookingProcess.`check meat freshness` } returns opMeatCheck
    coEvery { cookingProcess.`marinate meat` } returns opMeatMarinate
    coEvery { cookingProcess.`get grill from storage` } returns opGrillGet
    coEvery { cookingProcess.`check grill` } returns opGrillCheck
    coEvery { cookingProcess.`get sauce ingredients from storage` } returns opSauceGet
    coEvery { cookingProcess.`prepare sauce` } returns opSaucePrepare
    coEvery { cookingProcess.`get rosemary from storage` } returns opRosemaryGet
    coEvery { cookingProcess.`roast rosemary` } returns opRosemaryRoast
    coEvery { cookingProcess.`put meat on the grill and start roasting` } returns opMeatPrepare
    coEvery { cookingProcess.`roast meat` } returns opMeatRoast
    coEvery { cookingProcess.`serve steak` } returns opMeatServe
    coEvery { cookingProcess.`final check and create ribeye steak` } returns opFinish

    context("checking sequence of operations") {
        cookingProcess.run(mockk<ResourceRepository>())
        pos("prepare meat must be sequential") {
            coVerifyOrder {
                opMeatGet(any())
                opMeatCheck(any())
                opMeatMarinate(any())
            }
        }
        pos("prepare grill must be sequential") {
            coVerifyOrder {
                opGrillGet(any())
                opGrillCheck(any())
            }
        }
        pos("prepare sauce must be sequential") {
            coVerifyOrder {
                opSauceGet(any())
                opSaucePrepare(any())
            }
        }
        pos("prepare rosemary must be sequential") {
            coVerifyOrder {
                opRosemaryGet(any())
                opRosemaryRoast(any())
            }
        }
    }

    context("checking `prepare meat and grill`") {
        val meatState = mockk<State<Op.Meat.Marinate>>()
        val grillState = mockk<State<Op.Grill.Check>>()
        every { meatState.opType } returns Op.Meat.Marinate::class
        every { grillState.opType } returns Op.Grill.Check::class

        pos("must execute meat and grill in parallel and return combined result") {
            runTest {
                val meatMock = mockk<suspend () -> Result<State<Op.Meat.Marinate>>>()
                val grillMock = mockk<suspend () -> Result<State<Op.Grill.Check>>>()
                val combineMock = mockk<Op.Meat.PrepareForRoasting>()

                val finalState = Result.success(mockk<State<Op.Meat.PrepareForRoasting>>())

                coEvery { meatMock() } coAnswers {
                    delay(100.milliseconds)
                    Result.success(meatState)
                }
                coEvery { grillMock() } coAnswers {
                    delay(50.milliseconds)
                    Result.success(grillState)
                }
                coEvery { combineMock(any(), any()) } returns finalState

                `prepare meat and grill`(meatMock, grillMock, combineMock) shouldBe finalState
                coVerify(exactly = 1) { meatMock() }
                coVerify(exactly = 1) { grillMock() }
                coVerify(exactly = 1) { combineMock(meatState, grillState) }
            }
        }

        neg("must return first failure and short-circuit further processing") {
            runTest {
                val meatMock = mockk<suspend () -> Result<State<Op.Meat.Marinate>>>()
                val grillMock = mockk<suspend () -> Result<State<Op.Grill.Check>>>()
                val combineMock = mockk<Op.Meat.PrepareForRoasting>()
                val err = RuntimeException("Grill error")

                // Grill завершится раньше meat с ошибкой
                coEvery { grillMock() } coAnswers {
                    delay(100.milliseconds)
                    Result.failure(err)
                }

                // Meat выполняется дольше grill
                coEvery { meatMock() } coAnswers {
                    delay(200.milliseconds)
                    Result.success(meatState)
                }

                `prepare meat and grill`(meatMock, grillMock, combineMock) shouldBeFailure err
                coVerify(exactly = 0) { combineMock(any(), any()) }
            }
        }

        /* 1. Запускается процесс подготовки мяса длительностью 2 с.
           2. Запускается процесс подготовки гриля, который завершается с ошибкой через 100 мс и
              должен убить 1-ый процесс.
           3. Проверка, что 1-ый процесс завершился принудительно. */
        neg("must cancel pending jobs when one fails") {
            runTest {
                var meatWasCancelled = false
                val meatMock: suspend () -> Result<State<Op.Meat.Marinate>> = {
                    try {
                        delay(2.seconds)
                        Result.success(meatState)
                    } catch (e: CancellationException) {
                        meatWasCancelled = true
                        throw e
                    }
                }

                val grillMock = mockk<suspend () -> Result<State<Op.Grill.Check>>>()
                coEvery { grillMock() } coAnswers {
                    delay(100.milliseconds)
                    Result.failure(RuntimeException("Fail"))
                }

                `prepare meat and grill`(meatMock, grillMock, mockk(relaxed = true))

                advanceUntilIdle() // сдвиг времени, чтобы поймать исключение
                meatWasCancelled shouldBe true
            }
        }
    }

    context("checking `prepare sauce and rosemary`") {
        val meatState = mockk<State<Op.Meat.Roast>>()
        every { meatState.opType } returns Op.Meat.Roast::class
        val sauceState = mockk<State<Op.Sauce.Prepare>>()
        every { sauceState.opType } returns Op.Sauce.Prepare::class
        val rosemaryState = mockk<State<Op.Rosemary.Roast>>()
        every { rosemaryState.opType } returns Op.Rosemary.Roast::class

        pos("must execute meat, sauce and rosemary and return serve result") {
            runTest {
                val meatMock = mockk<suspend () -> Result<State<Op.Meat.Roast>>>()
                val sauceMock = mockk<suspend () -> Result<State<Op.Sauce.Prepare>>>()
                val rosemaryMock = mockk<suspend () -> Result<State<Op.Rosemary.Roast>>>()
                val serveMock = mockk<Op.Meat.Serve>()

                val finalState = Result.success(mockk<State<Op.Meat.Serve>>())
                coEvery { meatMock() } coAnswers {
                    delay(100.milliseconds)
                    Result.success(meatState)
                }
                coEvery { sauceMock() } coAnswers {
                    delay(100.milliseconds)
                    Result.success(sauceState)
                }
                coEvery { rosemaryMock() } coAnswers {
                    delay(100.milliseconds)
                    Result.success(rosemaryState)
                }
                coEvery { serveMock(any(), any(), any()) } returns finalState

                `prepare sauce and rosemary`(
                    meatMock,
                    sauceMock,
                    rosemaryMock,
                    serveMock
                ) shouldBe finalState
                currentTime shouldBe 100
            }
        }

        neg("failure of meat must cancel sauce and rosemary") {
            runTest {
                var sauceCancelled = false
                var rosemaryCancelled = false
                val sauceMock = mockk<suspend () -> Result<State<Op.Sauce.Prepare>>>()
                val rosemaryMock = mockk<suspend () -> Result<State<Op.Rosemary.Roast>>>()

                // MockK issue:
                val meatMock: suspend () -> Result<State<Op.Meat.Roast>> = {
                    Result.failure(RuntimeException("Meat failed"))
                }

                coEvery { sauceMock() } coAnswers {
                    try {
                        delay(1.seconds)
                        Result.success(sauceState)
                    } catch (e: CancellationException) {
                        sauceCancelled = true
                        throw e // Re-throw to complete cancellation process internally
                    }
                }
                coEvery { rosemaryMock() } coAnswers {
                    try {
                        delay(2.seconds)
                        Result.success(rosemaryState)
                    } catch (e: CancellationException) {
                        rosemaryCancelled = true
                        throw e // Re-throw to complete cancellation process internally
                    }
                }

                `prepare sauce and rosemary`(meatMock, sauceMock, rosemaryMock,mockk(relaxed = true))

                sauceCancelled shouldBe true
                rosemaryCancelled shouldBe true
                currentTime shouldBe 0
            }
        }
    }
})
