package ru.it_arch.tools.samples.ribeye.dsl

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

public inline infix fun <O : Op, P : Op> Result<OpResult<O>>.next(
    op: (OpResult<O>) -> Result<OpResult<P>>
): Result<OpResult<P>> =
    getOrNull()?.let { op(it) } ?: Result.failure(exceptionOrNull()!!)


// нужен список аргументов операций
// На входе: список операций
// Явный кастинг, т.к. возвращаемый тип функции должен быть списком с общей абстракцией, а каждая
// операция возвращает конкретный тип подмножества Op.
@Suppress("UNCHECKED_CAST")
public suspend inline fun <O : Op> split(
    vararg opsBlock: suspend () -> Result<OpResult<out O>>
): List<Result<OpResult<out O>>> = coroutineScope {
    val deferreds = opsBlock.map { op ->
        async { op() /*as Result<OpResult<O>>*/ }
    }
    deferreds.awaitAll()
}

public suspend fun <O : Op> splitAndCancelFirst(
    vararg opsBlock: suspend () -> Result<OpResult<out O>>
): Result<OpResult<O>> = coroutineScope {


    TODO()
}

/*
suspend fun splitAndCancelFirst(tasksHandlers: List<suspend () -> Result<String>>): Result<List<String>> = coroutineScope {
    val channel = Channel<Result<String>>(tasksHandlers.size) // Result<OpResult<T>>
    val jobs = tasksHandlers.map { task ->
        launch { channel.send(task()) }
    }

    val results = mutableListOf<String>()
    var firstFailure: Throwable? = null

    repeat(tasksHandlers.size) {
        if (firstFailure != null) return@repeat
        channel.receive().fold(
            onSuccess = results::add,
            onFailure = { err ->
                firstFailure = err // Захват любой первой ошибки
                jobs.forEach { it.cancel() } // отмена выполнения всех задач по отдельности
            }
        )
    }
    firstFailure?.let { Result.failure(it) } ?: Result.success(results.toList())
}

* */

