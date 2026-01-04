package ru.it_arch.tools.samples.ribeye.dsl

import ru.it_arch.k3dm.ValueObject
import kotlin.reflect.KClass
import kotlin.time.Instant

public sealed interface Event {
    public interface OpComplited<T : CookingProcess.Op> : ValueObject.Data, Event {
        public val type: KClass<T>
        public val result: Result<ResourceState>
        public val timestamp: Instant

        override fun validate() {}
    }
}
