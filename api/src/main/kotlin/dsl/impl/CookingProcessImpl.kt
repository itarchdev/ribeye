package ru.it_arch.tools.samples.ribeye.dsl.impl

import ru.it_arch.k3dm.ValueObject
import ru.it_arch.tools.samples.ribeye.dsl.CookingProcess
import ru.it_arch.tools.samples.ribeye.dsl.Event
import ru.it_arch.tools.samples.ribeye.dsl.ResourceState
import ru.it_arch.tools.samples.ribeye.dsl.impl.OpCompletedImpl.Companion.opCompleted
import ru.it_arch.tools.samples.ribeye.storage.Storage

@ConsistentCopyVisibility
public data class CookingProcessImpl private constructor(
    override val `get meat from storage`: CookingProcess.Op.GetMeat,
    override val `check meat freshness`: CookingProcess.Op.CheckMeat,
    override val `marinate meat`: CookingProcess.Op.MarinateMeat,
    override val `roast meat`: CookingProcess.Op.RoastMeat,
    override val `get grill from storage`: CookingProcess.Op.GetGrill,
    override val `check grill`: CookingProcess.Op.CheckGrill,
    override val `get sauce ingredients from storage`: CookingProcess.Op.GetSauceIngredients,
    override val `prepare sauce`: CookingProcess.Op.PrepareSauce,
    override val `get rosemary from storage`: CookingProcess.Op.GetRosemary,
    override val `roast rosemary`: CookingProcess.Op.RoastRosemary,
    override val `serve ribeye steak`: CookingProcess.Op.Finish
) : CookingProcess {

    init {
        validate()
    }

    override fun <T : ValueObject.Data> fork(vararg args: Any?): T {
        TODO("Not used")
    }

    public class Builder() {
        public var getMeat: (suspend (Storage) -> Result<ResourceState.Meat>)? = null
        public var checkMeat: ((ResourceState.Meat) -> Result<ResourceState.Meat>)? = null
        public var marinate: ((ResourceState.Meat) -> Result<ResourceState.Meat>)? = null
        public var roastMeat: ((ResourceState.Meat) -> Result<ResourceState.Meat>)? = null
        public var getGrill: ((Storage) -> Result<ResourceState.Grill>)? = null
        public var checkGrill: ((ResourceState.Grill) -> Result<ResourceState.Grill>)? = null
        public var getSauceIngredients: ((Storage) -> Result<ResourceState.Sauce>)? = null
        public var prepareSauce: ((ResourceState.Sauce) -> Result<ResourceState.Sauce>)? = null
        public var getRosemary: ((Storage) -> Result<ResourceState.Rosemary>)? = null
        public var roastRosemary: ((ResourceState.Rosemary) -> Result<ResourceState.Rosemary>)? = null
        public var finish: ((ResourceState.Meat, ResourceState.Sauce?, ResourceState.Rosemary?) -> Result<ResourceState.Steak>)? = null
        public var listener: ((Event.OpComplited<CookingProcess.Op>) -> Unit)? = null

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
                `get sauce ingredients from storage` = GetSauceIngredientsWrapper(getSauceIngredients!!, listener),
                `prepare sauce` = PrepareSauceWrapper(prepareSauce!!, listener),
                `get rosemary from storage` = GetRosemaryWrapper(getRosemary!!, listener),
                `roast rosemary` = RoastRosemaryWrapper(roastRosemary!!, listener),
                `serve ribeye steak` = FinishWrapper(finish!!, listener)
            )
        }
    }

    private class GetMeatWrapper(
        private val getMeat: CookingProcess.Op.GetMeat,
        private val listener: ((Event.OpComplited<CookingProcess.Op>) -> Unit)?
    ) : CookingProcess.Op.GetMeat by getMeat {

        override suspend fun invoke(storage: Storage): Result<ResourceState.Meat> =
            getMeat(storage).also { result -> listener?.let { it(opCompleted(result)) } }
    }

    private class CheckMeatWrapper(
        private val checkMeat: CookingProcess.Op.CheckMeat,
        private val listener: ((Event.OpComplited<CookingProcess.Op>) -> Unit)?
    ) : CookingProcess.Op.CheckMeat by checkMeat {

        override fun invoke(meat: ResourceState.Meat): Result<ResourceState.Meat> =
            checkMeat(meat).also { result -> listener?.let { it(opCompleted(result)) } }
    }

    private class MarinateWrapper(
        private val marinate: CookingProcess.Op.MarinateMeat,
        private val listener: ((Event.OpComplited<CookingProcess.Op>) -> Unit)?
    ): CookingProcess.Op.MarinateMeat by marinate {

        override fun invoke(meat: ResourceState.Meat): Result<ResourceState.Meat> =
            marinate(meat).also { result -> listener?.let { it(opCompleted(result)) } }
    }

    private class RoastMeatWrapper(
        private val roastMeat: CookingProcess.Op.RoastMeat,
        private val listener: ((Event.OpComplited<CookingProcess.Op>) -> Unit)?
    ) : CookingProcess.Op.RoastMeat by roastMeat {
        override fun invoke(meat: ResourceState.Meat): Result<ResourceState.Meat> =
            roastMeat(meat).also { result -> listener?.let { it(opCompleted(result)) } }
    }

    private class GetGrillWrapper(
        private val getGrill: CookingProcess.Op.GetGrill,
        private val listener: ((Event.OpComplited<CookingProcess.Op>) -> Unit)?
    ) : CookingProcess.Op.GetGrill by getGrill {

        override fun invoke(storage: Storage): Result<ResourceState.Grill> =
            getGrill(storage).also { result -> listener?.let { it(opCompleted(result)) } }
    }

    private class CheckGrillWrapper(
        private val checkGreill: CookingProcess.Op.CheckGrill,
        private val listener: ((Event.OpComplited<CookingProcess.Op>) -> Unit)?
    ) : CookingProcess.Op.CheckGrill by checkGreill {
        override fun invoke(grill: ResourceState.Grill): Result<ResourceState.Grill> =
            checkGreill(grill).also { result -> listener?.let { it(opCompleted(result)) } }
    }

    private class GetSauceIngredientsWrapper(
        private val getSauceIngredients: CookingProcess.Op.GetSauceIngredients,
        private val listener: ((Event.OpComplited<CookingProcess.Op>) -> Unit)?
    ) : CookingProcess.Op.GetSauceIngredients by getSauceIngredients {

        override fun invoke(storage: Storage): Result<ResourceState.Sauce> =
            getSauceIngredients(storage).also { result -> listener?.let { it(opCompleted(result)) } }
    }

    private class PrepareSauceWrapper(
        private val prepareSauce: CookingProcess.Op.PrepareSauce,
        private val listener: ((Event.OpComplited<CookingProcess.Op>) -> Unit)?
    ) : CookingProcess.Op.PrepareSauce by prepareSauce {
        override fun invoke(sauce: ResourceState.Sauce): Result<ResourceState.Sauce> =
            prepareSauce(sauce).also { result -> listener?.let { it(opCompleted(result)) } }
    }

    private class GetRosemaryWrapper(
        private val getRosemary: CookingProcess.Op.GetRosemary,
        private val listener: ((Event.OpComplited<CookingProcess.Op>) -> Unit)?
    ) : CookingProcess.Op.GetRosemary by getRosemary {

        override fun invoke(storage: Storage): Result<ResourceState.Rosemary> =
            getRosemary(storage).also { result -> listener?.let { it(opCompleted(result)) } }
    }

    private class RoastRosemaryWrapper(
        private val roastRosemary: CookingProcess.Op.RoastRosemary,
        private val listener: ((Event.OpComplited<CookingProcess.Op>) -> Unit)?
    ) : CookingProcess.Op.RoastRosemary by roastRosemary {
        override fun invoke(rosemary: ResourceState.Rosemary): Result<ResourceState.Rosemary> =
            roastRosemary(rosemary).also { result -> listener?.let { it(opCompleted(result)) } }
    }

    private class FinishWrapper(
        private val finish: CookingProcess.Op.Finish,
        private val listener: ((Event.OpComplited<CookingProcess.Op>) -> Unit)?
    ) : CookingProcess.Op.Finish by finish {
        override fun invoke(
            meat: ResourceState.Meat,
            sauce: ResourceState.Sauce?,
            rosemary: ResourceState.Rosemary
        ): Result<ResourceState.Steak> =
            finish(meat, sauce, rosemary)
                .also { result -> listener?.let { it(opCompleted(result)) } }
    }
}
