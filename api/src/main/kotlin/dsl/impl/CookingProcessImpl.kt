package ru.it_arch.tools.samples.ribeye.dsl.impl

import ru.it_arch.k3dm.ValueObject
import ru.it_arch.tools.samples.ribeye.ResourceRepository
import ru.it_arch.tools.samples.ribeye.data.Resource
import ru.it_arch.tools.samples.ribeye.dsl.CookingProcess
import ru.it_arch.tools.samples.ribeye.dsl.Event
import ru.it_arch.tools.samples.ribeye.dsl.Op
import ru.it_arch.tools.samples.ribeye.dsl.State
import ru.it_arch.tools.samples.ribeye.dsl.SteakReady
import ru.it_arch.tools.samples.ribeye.dsl.impl.OpCompletedImpl.Companion.opCompleted

@ConsistentCopyVisibility
public data class CookingProcessImpl private constructor(
    override val `get meat from storage`: Op.Meat.Get,
    override val `check meat freshness`: Op.Meat.Check,
    override val `marinate meat`: Op.Meat.Marinate,
    override val `get grill from storage`: Op.Grill.Get,
    override val `check grill`: Op.Grill.Check,
    override val `get sauce ingredients from storage`: Op.Sauce.Get,
    override val `prepare sauce`: Op.Sauce.Prepare,
    override val `get rosemary from storage`: Op.Rosemary.Get,
    override val `roast rosemary`: Op.Rosemary.Roast,
    override val `put meat on the grill`: Op.Meat.PrepareForRoasting,
    override val `roast meat`: Op.Meat.Roast,
    override val `serve steak`: Op.Meat.Serve,
    override val `final check and create ribeye steak`: Op.Finish
) : CookingProcess {

    init {
        validate()
    }

    override fun <T : ValueObject.Data> fork(vararg args: Any?): T {
        TODO("Not used")
    }

    public class Builder() {
        public var getMeat: (suspend (ResourceRepository) -> Result<State<Op.Meat.Get>>)? = null
        public var checkMeat: (suspend (State<Op.Meat.Get>) -> Result<State<Op.Meat.Check>>)? = null
        public var marinate: (suspend (State<Op.Meat.Check>) -> Result<State<Op.Meat.Marinate>>)? = null
        public var getGrill: (suspend (ResourceRepository) -> Result<State<Op.Grill.Get>>)? = null
        public var checkGrill: (suspend (State<Op.Grill.Get>) -> Result<State<Op.Grill.Check>>)? = null
        public var getSauceIngredients: (suspend (ResourceRepository) -> Result<State<Op.Sauce.Get>>)? = null
        public var prepareSauce: (suspend (State<Op.Sauce.Get>) -> Result<State<Op.Sauce.Prepare>>)? = null
        public var getRosemary: (suspend (ResourceRepository) -> Result<State<Op.Rosemary.Get>>)? = null
        public var roastRosemary: (suspend (State<Op.Rosemary.Get>) -> Result<State<Op.Rosemary.Roast>>)? = null
        public var steakStart: (suspend (State<Op.Meat.Marinate>, State<Op.Grill.Check>) -> Result<State<Op.Meat.PrepareForRoasting>>)? = null
        public var steakRoast: (suspend (State<Op.Meat.PrepareForRoasting>) -> Result<State<Op.Meat.Roast>>)? = null
        public var serve: (suspend (Result<State<Op.Meat.Roast>>, Result<State<Op.Sauce.Prepare>>, Result<State<Op.Rosemary.Roast>>) -> Result<State<Op.Meat.Serve>>)? = null
        public var finish: (suspend (State<Op.Meat.Serve>) -> Result<SteakReady>)? = null
        public var listener: ((Event.OpCompleted<out Op>) -> Unit)? = null

        public fun build(): CookingProcess {
            requireNotNull(getMeat) { "getMeat op must be set" }
            requireNotNull(checkMeat) { "checkMeat op must be set" }
            requireNotNull(marinate) { "marinate op must be set" }
            requireNotNull(getGrill) { "getGrill op must be set" }
            requireNotNull(checkGrill) { "checkGrill op must be set" }
            requireNotNull(getSauceIngredients) { "getSauceIngredients op must be set" }
            requireNotNull(prepareSauce) { "prepareSauce op must be set" }
            requireNotNull(getRosemary) { "getRosemary op must be set" }
            requireNotNull(roastRosemary) { "roastRosemary op must be set" }
            requireNotNull(steakStart) { "steakStart op must be set" }
            requireNotNull(steakRoast) { "steakRoast op must be set" }
            requireNotNull(serve) { "combine op must be set" }
            requireNotNull(finish) { "finish op must be set" }

            @Suppress("UNCHECKED_CAST")
            return CookingProcessImpl(
                `get meat from storage` = GetMeatWrapper(getMeat!!, listener),
                `check meat freshness` = CheckMeatWrapper(checkMeat!!, listener),
                `marinate meat` = MarinateWrapper(marinate!!, listener),
                `get grill from storage` = GetGrillWrapper(getGrill!!, listener),
                `check grill` = CheckGrillWrapper(checkGrill!!, listener),
                `get sauce ingredients from storage` = GetSauceWrapper(getSauceIngredients!!, listener),
                `prepare sauce` = PrepareSauceWrapper(prepareSauce!!, listener),
                `get rosemary from storage` = GetRosemaryWrapper(getRosemary!!, listener),
                `roast rosemary` = RoastRosemaryWrapper(roastRosemary!!, listener),
                `put meat on the grill` = SteakStartWrapper(steakStart!!, listener),
                `roast meat` = SteakRoastWrapper(steakRoast!!, listener!!),
                `serve steak` = ServeWrapper(serve!!, listener),
                `final check and create ribeye steak` = FinishWrapper(finish!!, listener)
            )
        }
    }

    private class GetMeatWrapper(
        private val getMeat: Op.Meat.Get,
        private val listener: ((Event.OpCompleted<Op.Meat.Get>) -> Unit)?
    ) : Op.Meat.Get by getMeat {

        override suspend fun invoke(storage: ResourceRepository): Result<State<Op.Meat.Get>> =
            getMeat(storage).also { result -> listener?.let { it(opCompleted(result)) } }
    }

    private class CheckMeatWrapper(
        private val checkMeat: Op.Meat.Check,
        private val listener: ((Event.OpCompleted<Op.Meat.Check>) -> Unit)?
    ) : Op.Meat.Check by checkMeat {

        override suspend fun invoke(meat: State<Op.Meat.Get>): Result<State<Op.Meat.Check>> =
            checkMeat(meat).also { result -> listener?.let { it(opCompleted(result)) } }
    }

    private class MarinateWrapper(
        private val marinate: Op.Meat.Marinate,
        private val listener: ((Event.OpCompleted<Op.Meat.Marinate>) -> Unit)?
    ) : Op.Meat.Marinate by marinate {

        override suspend fun invoke(meat: State<Op.Meat.Check>): Result<State<Op.Meat.Marinate>> =
            marinate(meat).also { result -> listener?.let { it(opCompleted(result)) } }
    }

    private class GetGrillWrapper(
        private val getGrill: Op.Grill.Get,
        private val listener: ((Event.OpCompleted<Op.Grill.Get>) -> Unit)?
    ) : Op.Grill.Get by getGrill {

        override suspend fun invoke(storage: ResourceRepository): Result<State<Op.Grill.Get>> =
            getGrill(storage).also { result -> listener?.let { it(opCompleted(result)) } }
    }

    private class CheckGrillWrapper(
        private val checkGreill: Op.Grill.Check,
        private val listener: ((Event.OpCompleted<Op.Grill.Check>) -> Unit)?
    ) : Op.Grill.Check by checkGreill {
        override suspend fun invoke(grill: State<Op.Grill.Get>): Result<State<Op.Grill.Check>> =
            checkGreill(grill).also { result -> listener?.let { it(opCompleted(result)) } }
    }

    private class GetSauceWrapper(
        private val getSauce: Op.Sauce.Get,
        private val listener: ((Event.OpCompleted<Op.Sauce.Get>) -> Unit)?
    ) : Op.Sauce.Get by getSauce {

        override suspend fun invoke(storage: ResourceRepository): Result<State<Op.Sauce.Get>> =
            getSauce(storage).also { result -> listener?.let { it(opCompleted(result)) } }
    }

    private class PrepareSauceWrapper(
        private val prepareSauce: Op.Sauce.Prepare,
        private val listener: ((Event.OpCompleted<Op.Sauce.Prepare>) -> Unit)?
    ) : Op.Sauce.Prepare by prepareSauce {
        override suspend fun invoke(sauce: State<Op.Sauce.Get>): Result<State<Op.Sauce.Prepare>> =
            prepareSauce(sauce).also { result -> listener?.let { it(opCompleted(result)) } }
    }

    private class GetRosemaryWrapper(
        private val getRosemary: Op.Rosemary.Get,
        private val listener: ((Event.OpCompleted<Op.Rosemary.Get>) -> Unit)?
    ) : Op.Rosemary.Get by getRosemary {

        override suspend fun invoke(storage: ResourceRepository): Result<State<Op.Rosemary.Get>> =
            getRosemary(storage).also { result -> listener?.let { it(opCompleted(result)) } }
    }

    private class RoastRosemaryWrapper(
        private val roastRosemary: Op.Rosemary.Roast,
        private val listener: ((Event.OpCompleted<Op.Rosemary.Roast>) -> Unit)?
    ) : Op.Rosemary.Roast by roastRosemary {
        override suspend fun invoke(rosemary: State<Op.Rosemary.Get>): Result<State<Op.Rosemary.Roast>> =
            roastRosemary(rosemary).also { result -> listener?.let { it(opCompleted(result)) } }
    }

    private class SteakStartWrapper(
        private val steak: Op.Meat.PrepareForRoasting,
        private val listener: ((Event.OpCompleted<Op.Meat.PrepareForRoasting>) -> Unit)?
    ) : Op.Meat.PrepareForRoasting by steak {
        override suspend fun invoke(
            meat: State<Op.Meat.Marinate>,
            grill: State<Op.Grill.Check>
        ): Result<State<Op.Meat.PrepareForRoasting>> =
            steak(meat, grill).also { result -> listener?.let { it(opCompleted(result)) } }
    }

    private class SteakRoastWrapper(
        private val roast: Op.Meat.Roast,
        private val listener: ((Event.OpCompleted<Op.Meat.Roast>) -> Unit)?
    ) : Op.Meat.Roast by roast {
        override suspend fun invoke(steak: State<Op.Meat.PrepareForRoasting>): Result<State<Op.Meat.Roast>> =
            roast(steak).also { result -> listener?.let { it(opCompleted(result)) } }
    }

    private class ServeWrapper(
        private val serve: Op.Meat.Serve,
        private val listener: ((Event.OpCompleted<Op.Meat.Serve>) -> Unit)?
    ) : Op.Meat.Serve by serve {
        override suspend fun invoke(
            steak: Result<State<Op.Meat.Roast>>,
            sauce: Result<State<Op.Sauce.Prepare>>,
            rosemary: Result<State<Op.Rosemary.Roast>>
        ): Result<State<Op.Meat.Serve>> =
            serve(steak, sauce, rosemary)
                .also { result -> listener?.let { it(opCompleted(result)) } }
    }

    private class FinishWrapper(
        private val finish: Op.Finish,
        private val listener: ((Event.OpCompleted<Op.Finish>) -> Unit)?
    ) : Op.Finish by finish {
        override suspend fun invoke(steak: State<Op.Meat.Serve>): Result<SteakReady> =
            finish(steak).also { result -> listener?.let { it(opCompleted(result)) } }
    }
}
