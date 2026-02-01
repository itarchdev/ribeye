package ru.it_arch.tools.samples.ribeye.bl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.kotest.provided.neg
import io.kotest.provided.pos
import ru.it_arch.tools.samples.ribeye.dsl.Op
import ru.it_arch.tools.samples.ribeye.dsl.ValueChain
import ru.it_arch.tools.samples.ribeye.dsl.impl.MacronutrientsImpl
import ru.it_arch.tools.samples.ribeye.dsl.impl.checkGrillState
import ru.it_arch.tools.samples.ribeye.dsl.impl.checkMeatState
import ru.it_arch.tools.samples.ribeye.dsl.impl.getGrillState
import ru.it_arch.tools.samples.ribeye.dsl.impl.getMeatState
import ru.it_arch.tools.samples.ribeye.dsl.impl.getSauceIngredientsState
import ru.it_arch.tools.samples.ribeye.dsl.impl.grill
import ru.it_arch.tools.samples.ribeye.dsl.impl.marinateState
import ru.it_arch.tools.samples.ribeye.dsl.impl.meat
import ru.it_arch.tools.samples.ribeye.dsl.impl.sauceIngredients
import ru.it_arch.tools.samples.ribeye.dsl.impl.toBuilder
import ru.it_arch.tools.samples.ribeye.dsl.impl.toDslBuilder
import ru.it_arch.tools.samples.ribeye.dsl.impl.valueChain
import ru.it_arch.tools.samples.ribeye.dsl.impl.weight
import javax.crypto.Mac
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class BusinessLogicTest : FunSpec({
    pos("simpleValueChain must calculate correct value chain for overhead cost factor and fixed overhead ") {

        // expected: (17 * 60) * (1300 / 3600) * (1 + 0.21) + 20
        // 1020 * 0.3611... * 1.21 + 20
        // 368.333...   445.6833.. + 20 = 465.6833..
        //val expected = 465.68.toBigDecimal().setScale(ValueChain.SCALE).let { ValueChainImpl(it) }
        val expected = 465.68.valueChain

        val result = 17.minutes.simpleValueChain(0.21, 20.valueChain)
        result shouldBe expected
        result.boxed.scale() shouldBe ValueChain.SCALE
    }

    context("stateForGetMeat") {
        val meatOk = meat {
            macronutrients = MacronutrientsImpl.DEFAULT
            quantity = 370
            expiration = Clock.System.now() + 10.days
        }

        pos("must check parameters and create correct state") {

            val expected = getMeatState {
                opType = Op.Meat.Get::class
                macronutrients = MacronutrientsImpl.DEFAULT
                quantity = 370.weight
                elapsed = 2.minutes
                // (2 min * 60) * (1300 / 3600) * (1 + 0.15) + 20 =
                // 120 * 0.36111 * 1.15 + 20 = 49.8333 + 20 = 69.8333
                value = 69.83.valueChain
            }

            meatOk.stateForGetMeat(2.minutes) shouldBe expected
        }

        neg("must throw exception if meat is rotten") {
            val meat = meatOk.toDslBuilder()
                .apply { expiration = Clock.System.now() - 10.days }.build()
            val exception = shouldThrow<IllegalArgumentException> {
                meat.stateForGetMeat(2.minutes)
            }
            exception.message shouldStartWith "Meat is rotten."
        }

        neg("must throw exception if weight is out of range") {
            val meat = meatOk.toDslBuilder().apply { quantity = 300 }.build()
            val exception = shouldThrow<IllegalArgumentException> {
                meat.stateForGetMeat(2.minutes)
            }
            exception.message shouldStartWith "Meat acceptance: weight must be in range"
        }

        neg("must throw exception if duration is out of range") {
            val exception = shouldThrow<IllegalArgumentException> {
                meatOk.stateForGetMeat(5.minutes)
            }
            exception.message shouldStartWith "Meat acceptance: duration must be in range"
        }
    }

    context("stateForCheckMeat") {
        val meatStateGet = getMeatState {
            opType = Op.Meat.Get::class
            macronutrients = MacronutrientsImpl.DEFAULT.toDslBuilder().apply { fats = 20.0 }.build()
            quantity = 370.weight
            elapsed = 2.minutes
            value = 70.valueChain
        }

        pos("must check parameters and create correct state") {
            val expected = checkMeatState {
                opType = Op.Meat.Check::class
                macronutrients = MacronutrientsImpl.DEFAULT.toDslBuilder().apply { fats = 20.0 }.build()
                quantity = 370.weight
                elapsed = 3.minutes
                // (3 * 60) * (1300 / 3600) * (1 + 0.2) + 25 = 180 * 0.36111 * 1.2 + 95 = 173
                value = 103.valueChain
            }
            meatStateGet.stateForCheckMeat(3.minutes) shouldBe expected
        }

        neg("must throw exception if fats are not in range [18..25]") {
            val meatStateErr = meatStateGet.toBuilder()
                .apply {
                    macronutrients = macronutrients!!.toDslBuilder().apply { fats = 10.0 }.build()
                }.build()
            val exception = shouldThrow<IllegalArgumentException> {
                meatStateErr.stateForCheckMeat(3.minutes)
            }
            exception.message shouldStartWith "Meat check: fats must be in range"
        }

        neg("must throw exception if duration is out of range") {
            val exception = shouldThrow<java.lang.IllegalArgumentException> {
                meatStateGet.stateForCheckMeat(7.minutes)
            }
            exception.message shouldStartWith "Meat check: duration must be in range"
        }
    }

    context("stateForMarinate") {
        val meatStateCheck = checkMeatState {
            opType = Op.Meat.Check::class
            macronutrients = MacronutrientsImpl.DEFAULT
            quantity = 370.weight
            elapsed = 7.minutes
            value = 180.valueChain
        }

        pos("must check parameters and create correct state") {
            val expected = marinateState {
                opType = Op.Meat.Marinate::class
                macronutrients = MacronutrientsImpl.DEFAULT
                quantity = 370.weight.addPercent(0.07)
                elapsed = 20.minutes
                // (20 * 60) * (1300 / 3600) * (1 + 0.75) + 20 =
                // 20 + 1200 * 0.36111.. * 1.75 = 200 + 758.333.. = 958.333..
                value = 778.33.valueChain
            }
            meatStateCheck.stateForMarinate(20.minutes) shouldBe expected
        }

        neg("must throw exception if duration is out of range") {
            val exception = shouldThrow<IllegalArgumentException> {
                meatStateCheck.stateForMarinate(10.minutes)
            }
            exception.message shouldStartWith "Marinate: duration must be in range"
        }
    }

    context("stateForGetGrill") {
        val grillOk = grill {
            macronutrients = MacronutrientsImpl.DEFAULT
            quantity = 700
            expiration = Instant.DISTANT_FUTURE
        }

        pos("must check parameters and create correct state") {
            val expected = getGrillState {
                opType = Op.Grill.Get::class
                macronutrients = MacronutrientsImpl.DEFAULT
                quantity = 700.weight
                elapsed = 1.minutes
                // (1 min * 60) * (1300 / 3600) * (1 + 0.10) + 10 = 60 * 0.3611... * 1.1 + 10 = 33.833...
                value = 33.83.valueChain
            }
            grillOk.stateForGetGrill(1.minutes) shouldBe expected
        }

        neg("must throw exception if quantity is out of range") {
            val grillErr = grillOk.toDslBuilder().apply { quantity = 400 }.build()
            val exception = shouldThrow<IllegalArgumentException> {
                grillErr.stateForGetGrill(1.minutes)
            }
            exception.message shouldStartWith "Grill acceptance: weight must be in range"
        }

        neg("must throw exception if duration is out of range") {
            val exception = shouldThrow<IllegalArgumentException> {
                grillOk.stateForGetGrill(3.minutes)
            }
            exception.message shouldStartWith "Grill acceptance: duration mus be in range"
        }
    }

    context("stateForCheckGrill") {
        val grillStateGet = getGrillState {
            opType = Op.Grill.Get::class
            macronutrients = MacronutrientsImpl.DEFAULT
            quantity = 700.weight
            elapsed = 1.minutes
            value = 40.valueChain
        }

        pos("must check parameters and create correct state") {
            val expected = checkGrillState {
                opType = Op.Grill.Check::class
                macronutrients = MacronutrientsImpl.DEFAULT
                quantity = 700.weight
                elapsed = 35.minutes
                // (35 min * 60) * (1300 / 3600) * (1 + 0.23) + 30 = 2100 * 0.3611.. + 30 = 962.75
                value = 962.75.valueChain
            }
            grillStateGet.stateForCheckGrill(35.minutes) shouldBe expected
        }

        neg("must throw exception if duration is out of range") {
            val exception = shouldThrow<IllegalArgumentException> {
                grillStateGet.stateForCheckGrill(45.minutes)
            }
            exception.message shouldStartWith "Prepare grill: duration must be in range"
        }
    }

    context("stateForGetSauceIngredients") {
        val sauceIngredients = sauceIngredients {
            macronutrients = MacronutrientsImpl.DEFAULT
            quantity = 170
            expiration = Clock.System.now() + 30.days
        }

        pos("must check parameters and create correct state") {
            val expected = getSauceIngredientsState {
                opType = Op.Sauce.Get::class
                macronutrients = MacronutrientsImpl.DEFAULT
                quantity = 170.weight
                elapsed = 2.minutes
                // (2 min * 60) * (1300 / 3600) * (1 + 0.13) + 15 = 120 * 0.3611... + 15 = 63.966...
                value = 63.97.valueChain
            }
            sauceIngredients.stateForGetSauceIngredients(2.minutes) shouldBe expected
        }

        neg("must throw exception if quantity is out of range") {
            val sauceErr = sauceIngredients.toDslBuilder().apply { quantity = 100 }.build()
            val exception = shouldThrow<IllegalArgumentException> {
                sauceErr.stateForGetSauceIngredients(2.minutes)
            }
            exception.message shouldStartWith "Sauce ingredients acceptance: weight must be in range"
        }

        neg("must throw exception if duration is out of range") {
            val exception = shouldThrow<IllegalArgumentException> {
                sauceIngredients.stateForGetSauceIngredients(5.minutes)
            }
            exception.message shouldStartWith "Sauce ingredients acceptance: duration must be in range"
        }
    }

    context("stateFor")

})
