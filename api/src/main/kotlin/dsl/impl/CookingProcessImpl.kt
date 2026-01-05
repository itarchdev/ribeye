package ru.it_arch.tools.samples.ribeye.dsl.impl

import ru.it_arch.k3dm.ValueObject
import ru.it_arch.tools.samples.ribeye.ResourceRepository
import ru.it_arch.tools.samples.ribeye.dsl.CookingProcess
import ru.it_arch.tools.samples.ribeye.dsl.Event
import ru.it_arch.tools.samples.ribeye.dsl.Op
import ru.it_arch.tools.samples.ribeye.dsl.OpResult
import ru.it_arch.tools.samples.ribeye.dsl.Steak
import ru.it_arch.tools.samples.ribeye.dsl.impl.OpCompletedImpl.Companion.opCompleted

@ConsistentCopyVisibility
public data class CookingProcessImpl private constructor(
    override val `get meat from storage`: Op.Meat.Get,
    override val `check meat freshness`: Op.Meat.Check,
    override val `marinate meat`: Op.Meat.Marinate,
    override val `roast meat`: Op.Meat.Roast,
    override val `get grill from storage`: Op.Grill.Get,
    override val `check grill`: Op.Grill.Check,
    override val `get sauce ingredients from storage`: Op.Sauce.Get,
    override val `prepare sauce`: Op.Sauce.Prepare,
    override val `get rosemary from storage`: Op.Rosemary.Get,
    override val `roast rosemary`: Op.Rosemary.Roast,
    override val `serve ribeye steak`: Op.Finish
) : CookingProcess {

    init {
        validate()
    }

    override fun <T : ValueObject.Data> fork(vararg args: Any?): T {
        TODO("Not used")
    }

    public class Builder() {
        public var getMeat: (suspend (ResourceRepository) -> Result<OpResult<Op.Meat.Get>>)? = null
        public var checkMeat: ((OpResult<Op.Meat.Get>) -> Result<OpResult<Op.Meat.Check>>)? = null
        public var marinate: ((OpResult<Op.Meat.Check>) -> Result<OpResult<Op.Meat.Marinate>>)? = null
        public var roastMeat: ((OpResult<Op.Meat.Marinate>) -> Result<OpResult<Op.Meat.Roast>>)? = null
        public var getGrill: ((ResourceRepository) -> Result<OpResult<Op.Grill.Get>>)? = null
        public var checkGrill: ((OpResult<Op.Grill.Get>) -> Result<OpResult<Op.Grill.Check>>)? = null
        public var getSauceIngredients: ((ResourceRepository) -> Result<OpResult<Op.Sauce.Get>>)? = null
        public var prepareSauce: ((OpResult<Op.Sauce.Get>) -> Result<OpResult<Op.Sauce.Prepare>>)? = null
        public var getRosemary: ((ResourceRepository) -> Result<OpResult<Op.Rosemary.Get>>)? = null
        public var roastRosemary: ((OpResult<Op.Rosemary.Get>) -> Result<OpResult<Op.Rosemary.Roast>>)? = null
        public var finish: ((OpResult<Op.Meat.Roast>, OpResult<Op.Sauce.Prepare>?, OpResult<Op.Rosemary.Roast>?) -> Result<Steak>)? = null
        public var listener: ((Event.OpCompleted<out Op>) -> Unit)? = null

        public fun build(): CookingProcess {
            requireNotNull(getMeat) { "getMeat op must be set" }
            requireNotNull(checkMeat) { "checkMeat op must be set" }
            requireNotNull(marinate) { "marinate op must be set" }
            requireNotNull(roastMeat) { "roastMeat op must be set" }
            requireNotNull(getGrill) { "getGrill op must be set" }
            requireNotNull(checkGrill) { "checkGrill op must be set" }
            requireNotNull(getSauceIngredients) { "getSauceIngredients op must be set" }
            requireNotNull(prepareSauce) { "prepareSauce op must be set" }
            requireNotNull(getRosemary) { "getRosemary op must be set" }
            requireNotNull(roastRosemary) { "roastRosemary op must be set" }
            requireNotNull(finish) { "finish op must be set" }

            @Suppress("UNCHECKED_CAST")
            return CookingProcessImpl(
                `get meat from storage` = GetMeatWrapper(getMeat!!, listener),
                `check meat freshness` = CheckMeatWrapper(checkMeat!!, listener),
                `marinate meat` = MarinateWrapper(marinate!!, listener),
                `roast meat` = RoastMeatWrapper(roastMeat!!, listener),
                `get grill from storage` = GetGrillWrapper(getGrill!!, listener),
                `check grill` = CheckGrillWrapper(checkGrill!!, listener),
                `get sauce ingredients from storage` = GetSauceWrapper(getSauceIngredients!!, listener),
                `prepare sauce` = PrepareSauceWrapper(prepareSauce!!, listener),
                `get rosemary from storage` = GetRosemaryWrapper(getRosemary!!, listener),
                `roast rosemary` = RoastRosemaryWrapper(roastRosemary!!, listener),
                `serve ribeye steak` = FinishWrapper(finish!!, listener)
            )
        }
    }

    private class GetMeatWrapper(
        private val getMeat: Op.Meat.Get,
        private val listener: ((Event.OpCompleted<Op.Meat.Get>) -> Unit)?
    ) : Op.Meat.Get by getMeat {

        override suspend fun invoke(storage: ResourceRepository): Result<OpResult<Op.Meat.Get>> =
            getMeat(storage).also { result -> listener?.let { it(opCompleted(result)) } }
    }

    private class CheckMeatWrapper(
        private val checkMeat: Op.Meat.Check,
        private val listener: ((Event.OpCompleted<Op.Meat.Check>) -> Unit)?
    ) : Op.Meat.Check by checkMeat {

        override fun invoke(meat: OpResult<Op.Meat.Get>): Result<OpResult<Op.Meat.Check>> =
            checkMeat(meat).also { result -> listener?.let { it(opCompleted(result)) } }
    }

    private class MarinateWrapper(
        private val marinate: Op.Meat.Marinate,
        private val listener: ((Event.OpCompleted<Op.Meat.Marinate>) -> Unit)?
    ): Op.Meat.Marinate by marinate {

        override fun invoke(meat: OpResult<Op.Meat.Check>): Result<OpResult<Op.Meat.Marinate>> =
            marinate(meat).also { result -> listener?.let { it(opCompleted(result)) } }
    }

    private class RoastMeatWrapper(
        private val roastMeat: Op.Meat.Roast,
        private val listener: ((Event.OpCompleted<Op.Meat.Roast>) -> Unit)?
    ) : Op.Meat.Roast by roastMeat {
        override fun invoke(meat: OpResult<Op.Meat.Marinate>): Result<OpResult<Op.Meat.Roast>> =
            roastMeat(meat).also { result -> listener?.let { it(opCompleted(result)) } }
    }

    private class GetGrillWrapper(
        private val getGrill: Op.Grill.Get,
        private val listener: ((Event.OpCompleted<Op.Grill.Get>) -> Unit)?
    ) : Op.Grill.Get by getGrill {

        override fun invoke(storage: ResourceRepository): Result<OpResult<Op.Grill.Get>> =
            getGrill(storage).also { result -> listener?.let { it(opCompleted(result)) } }
    }

    private class CheckGrillWrapper(
        private val checkGreill: Op.Grill.Check,
        private val listener: ((Event.OpCompleted<Op.Grill.Check>) -> Unit)?
    ) : Op.Grill.Check by checkGreill {
        override fun invoke(grill: OpResult<Op.Grill.Get>): Result<OpResult<Op.Grill.Check>> =
            checkGreill(grill).also { result -> listener?.let { it(opCompleted(result)) } }
    }

    private class GetSauceWrapper(
        private val getSauce: Op.Sauce.Get,
        private val listener: ((Event.OpCompleted<Op.Sauce.Get>) -> Unit)?
    ) : Op.Sauce.Get by getSauce {

        override fun invoke(storage: ResourceRepository): Result<OpResult<Op.Sauce.Get>> =
            getSauce(storage).also { result -> listener?.let { it(opCompleted(result)) } }
    }

    private class PrepareSauceWrapper(
        private val prepareSauce: Op.Sauce.Prepare,
        private val listener: ((Event.OpCompleted<Op.Sauce.Prepare>) -> Unit)?
    ) : Op.Sauce.Prepare by prepareSauce {
        override fun invoke(sauce: OpResult<Op.Sauce.Get>): Result<OpResult<Op.Sauce.Prepare>> =
            prepareSauce(sauce).also { result -> listener?.let { it(opCompleted(result)) } }
    }

    private class GetRosemaryWrapper(
        private val getRosemary: Op.Rosemary.Get,
        private val listener: ((Event.OpCompleted<Op.Rosemary.Get>) -> Unit)?
    ) : Op.Rosemary.Get by getRosemary {

        override fun invoke(storage: ResourceRepository): Result<OpResult<Op.Rosemary.Get>> =
            getRosemary(storage).also { result -> listener?.let { it(opCompleted(result)) } }
    }

    private class RoastRosemaryWrapper(
        private val roastRosemary: Op.Rosemary.Roast,
        private val listener: ((Event.OpCompleted<Op.Rosemary.Roast>) -> Unit)?
    ) : Op.Rosemary.Roast by roastRosemary {
        override fun invoke(rosemary: OpResult<Op.Rosemary.Get>): Result<OpResult<Op.Rosemary.Roast>> =
            roastRosemary(rosemary).also { result -> listener?.let { it(opCompleted(result)) } }
    }

    private class FinishWrapper(
        private val finish: Op.Finish,
        private val listener: ((Event.OpCompleted<Op.Finish>) -> Unit)?
    ) : Op.Finish by finish {
        override fun invoke(
            meat: OpResult<Op.Meat.Roast>,
            sauce: OpResult<Op.Sauce.Prepare>?,
            rosemary: OpResult<Op.Rosemary.Roast>?
        ): Result<Steak> =
            finish(meat, sauce, rosemary)
                .also { result -> listener?.let { it(opCompleted(result)) } }
    }
}
