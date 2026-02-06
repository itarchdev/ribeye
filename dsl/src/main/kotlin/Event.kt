package ru.it_arch.tools.samples.ribeye

import ru.it_arch.k3dm.ValueObject
import kotlin.time.Clock
import kotlin.time.Instant

public sealed interface Event {
    @ConsistentCopyVisibility
    public data class OpCompleted<T : Op> private constructor(
        public val result: Result<State<T>>,
        public val timestamp: Instant = Clock.System.now()
    ): ValueObject.Data, Event {

        init {
            validate()
        }

        override fun validate() {}

        override fun <T : ValueObject.Data> fork(vararg args: Any?): T {
            TODO("Not used")
        }

        public companion object {
            public fun <T : Op> opCompleted(result: Result<State<T>>): OpCompleted<T> =
                OpCompleted(result)
        }
    }
}
