package ru.it_arch.tools.samples.ribeye

/*
public inline infix fun <IN : OpState, OUT : OpState> Result<IN>.next(op: (IN) -> Result<OUT>): Result<OUT> =
    getOrNull()?.let { op(it) } ?: Result.failure(exceptionOrNull()!!)
*/
