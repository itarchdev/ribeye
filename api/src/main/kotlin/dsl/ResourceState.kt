package ru.it_arch.tools.samples.ribeye.dsl

import ru.it_arch.k3dm.ValueObject
import ru.it_arch.tools.samples.ribeye.storage.Macronutrients
import ru.it_arch.tools.samples.ribeye.storage.Quantity
import kotlin.time.Duration

public sealed interface ResourceState : ValueObject.Data {
    public val macronutrients: Macronutrients
    public val quantity: Quantity // TODO: move to :api ???
    public val price: Price
    public val elapsed: Duration

    public interface Steak : ResourceState {
        public val meat: Meat
        public val sauce: Sauce?
        public val rosemary: Rosemary?

        override fun validate() {}
    }

    public interface Grill : ResourceState {
        public val state: State

        override fun validate() {}

        public fun setState(state: State): Grill

        public enum class State {
            INIT, READY
        }
    }

    public interface Meat : ResourceState {
        public val state: State

        override fun validate() {}

        public fun setState(state: State): Meat

        public enum class State {
            INIT, FREAH, MARINATED, ROASTED
        }
    }

    public interface Sauce : ResourceState {
        public val state: State

        override fun validate() {}

        public fun setState(state: State): Sauce

        public enum class State {
            INIT, READY
        }
    }

    public interface Rosemary : ResourceState {
        public val state: State

        override fun validate() {}

        public fun setState(state: State): Rosemary

        public enum class State {
            INIT, ROASTED
        }
    }
}
