package ru.it_arch.tools.samples.ribeye

import ru.it_arch.tools.samples.ribeye.dsl.ResourceState

public inline infix fun <IN : ResourceState, OUT : ResourceState> Result<IN>.next(op: (IN) -> Result<OUT>): Result<OUT> =
    getOrNull()?.let { op(it) } ?: Result.failure(exceptionOrNull()!!)
