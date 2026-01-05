package ru.it_arch.tools.samples.ribeye.dsl

import ru.it_arch.k3dm.ValueObject
import kotlin.time.Instant

public sealed interface Event {
    public interface OpCompleted<T : Op> : ValueObject.Data, Event {
        public val result: Result<OpResult<T>>
        public val timestamp: Instant

        override fun validate() {}
    }
}
