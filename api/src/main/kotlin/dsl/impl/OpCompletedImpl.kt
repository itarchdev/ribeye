package ru.it_arch.tools.samples.ribeye.dsl.impl

import ru.it_arch.k3dm.ValueObject
import ru.it_arch.tools.samples.ribeye.dsl.CookingProcess
import ru.it_arch.tools.samples.ribeye.dsl.Event
import ru.it_arch.tools.samples.ribeye.dsl.ResourceState
import kotlin.reflect.KClass
import kotlin.time.Clock
import kotlin.time.Instant

@ConsistentCopyVisibility
internal data class OpCompletedImpl<T : CookingProcess.Op> private constructor(
    override val type: KClass<T>,
    override val result: Result<ResourceState>,
    override val timestamp: Instant = Clock.System.now(),
): Event.OpComplited<T> {
    init {
        validate()
    }

    override fun <T : ValueObject.Data> fork(vararg args: Any?): T {
        TODO("Not used")
    }

    companion object {
        inline fun <reified T : CookingProcess.Op> opCompleted(result: Result<ResourceState>) =
            OpCompletedImpl(T::class, result)
    }
}
