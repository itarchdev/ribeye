package ru.it_arch.tools.samples.ribeye.dsl

import ru.it_arch.k3dm.ValueObject
import ru.it_arch.tools.samples.ribeye.storage.Storage

public interface CookingProcess : ValueObject.Data {
    public val `get meat from storage`: Op.GetMeat
    public val `check meat freshness`: Op.CheckMeat
    public val `marinate meat`: Op.MarinateMeat
    public val `roast meat`: Op.RoastMeat
    public val `get grill from storage`: Op.GetGrill
    public val `check grill`: Op.CheckGrill
    public val `get sauce ingredients from storage`: Op.GetSauceIngredients
    public val `prepare sauce`: Op.PrepareSauce
    public val `get rosemary from storage`: Op.GetRosemary
    public val `roast rosemary`: Op.RoastRosemary
    public val `serve ribeye steak`: Op.Finish

    override fun validate() {}

    public sealed interface Op {

        // Meat

        public fun interface GetMeat : Op {
            public operator suspend fun invoke(storage: Storage): Result<ResourceState.Meat>
        }

        public fun interface CheckMeat : Op {
            public operator fun invoke(meat: ResourceState.Meat): Result<ResourceState.Meat>
        }

        public fun interface MarinateMeat : Op {
            public operator fun invoke(meat: ResourceState.Meat): Result<ResourceState.Meat>
        }

        public fun interface RoastMeat : Op {
            public operator fun invoke(meat: ResourceState.Meat): Result<ResourceState.Meat>
        }

        // Grill

        public fun interface GetGrill : Op {
            public operator fun invoke(storage: Storage): Result<ResourceState.Grill>
        }

        public fun interface CheckGrill : Op {
            public operator fun invoke(grill: ResourceState.Grill): Result<ResourceState.Grill>
        }

        // Sauce

        public fun interface GetSauceIngredients : Op {
            public operator fun invoke(storage: Storage): Result<ResourceState.Sauce>
        }

        public fun interface PrepareSauce : Op {
            public operator fun invoke(sauce: ResourceState.Sauce): Result<ResourceState.Sauce>
        }

        // Rosemary

        public fun interface GetRosemary : Op {
            public operator fun invoke(storage: Storage): Result<ResourceState.Rosemary>
        }

        public fun interface RoastRosemary : Op {
            public operator fun invoke(rosemary: ResourceState.Rosemary): Result<ResourceState.Rosemary>
        }

        public fun interface Finish : Op {
            public operator fun invoke(
                meat: ResourceState.Meat,
                sauce: ResourceState.Sauce?,
                rosemary: ResourceState.Rosemary
            ): Result<ResourceState.Steak>
        }
    }
}
