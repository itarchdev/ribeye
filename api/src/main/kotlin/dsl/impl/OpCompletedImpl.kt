package ru.it_arch.tools.samples.ribeye.dsl.impl

import ru.it_arch.k3dm.ValueObject
import ru.it_arch.tools.samples.ribeye.dsl.Event
import ru.it_arch.tools.samples.ribeye.dsl.Op
import ru.it_arch.tools.samples.ribeye.dsl.OpResult
import kotlin.time.Clock
import kotlin.time.Instant

@ConsistentCopyVisibility
internal data class OpCompletedImpl<T : Op> private constructor(
    override val result: Result<OpResult<T>>,
    override val timestamp: Instant = Clock.System.now(),
) : Event.OpCompleted<T> {
    init {
        validate()
    }

    override fun <T : ValueObject.Data> fork(vararg args: Any?): T {
        TODO("Not used")
    }

    companion object {
        fun <T : Op> opCompleted(result: Result<OpResult<T>>): Event.OpCompleted<T> =
            OpCompletedImpl(result)
    }
}
