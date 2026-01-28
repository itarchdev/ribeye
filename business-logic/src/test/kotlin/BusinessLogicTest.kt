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
import ru.it_arch.tools.samples.ribeye.dsl.impl.getMeatState
import ru.it_arch.tools.samples.ribeye.dsl.impl.meat
import ru.it_arch.tools.samples.ribeye.dsl.impl.toDslBuilder
import ru.it_arch.tools.samples.ribeye.dsl.impl.valueChain
import ru.it_arch.tools.samples.ribeye.dsl.impl.weight
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

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
        neg("must throw exception of weight is out of range") {
            val meat = meatOk.toDslBuilder().apply { quantity = 300 }.build()
            val exception = shouldThrow<IllegalArgumentException> {
                meat.stateForGetMeat(2.minutes)
            }
            exception.message shouldStartWith "Meat acceptance: weight must be in range"
        }
        neg("must throw exception of duration is out of range") {
            val exception = shouldThrow<IllegalArgumentException> {
                meatOk.stateForGetMeat(5.minutes)
            }
            exception.message shouldStartWith "Meat acceptance: duration must be in range"
        }
    }
})
