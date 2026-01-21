package ru.it_arch.tools.samples.ribeye

import io.kotest.assertions.any
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.provided.neg
import io.kotest.provided.pos
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import ru.it_arch.tools.samples.ribeye.dsl.CookingProcess
import ru.it_arch.tools.samples.ribeye.dsl.Op
import ru.it_arch.tools.samples.ribeye.dsl.State
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.days

class CookingProcessScriptTest: FunSpec({

    fun <T : Op> defaultState(type: KClass<out T>): State<T> =
        State(
            opType = type,
            macronutrients = mockk(),
            quantity = mockk(),
            elapsed = 1.days,
            valueChain = mockk()
        )

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

    context("checking parallelism") {
        pos("must execute meat and grill in parallel and return combined result") {
            runTest {
                val meatMock = mockk<suspend () -> Result<State<Op.Meat.Marinate>>>()
                val grillMock = mockk<suspend () -> Result<State<Op.Grill.Check>>>()
                val combineMock = mockk<Op.Meat.PrepareForRoasting>()

                val meatState = mockk<State<Op.Meat.Marinate>>()
                every { meatState.opType } returns Op.Meat.Marinate::class
                val grillState = mockk<State<Op.Grill.Check>>()
                every { grillState.opType } returns Op.Grill.Check::class
                val finalState = Result.success(mockk<State<Op.Meat.PrepareForRoasting>>())

                coEvery { meatMock() } coAnswers {
                    delay(100) // Simulate work
                    Result.success(meatState)
                }
                coEvery { grillMock() } coAnswers {
                    delay(50) // Simulate work
                    Result.success(grillState)
                }
                coEvery { combineMock(any(), any()) } returns finalState

                // Act
                `prepare meat and grill`(meatMock, grillMock, combineMock) shouldBe finalState
                coVerify(exactly = 1) { meatMock() }
                coVerify(exactly = 1) { grillMock() }
                coVerify(exactly = 1) { combineMock(meatState, grillState) }
            }
        }
    }
})
