package ru.it_arch.tools.samples.ribeye.dsl

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Функция-связка между последовательными операциями.
 * */
public inline infix fun <O : Op, P : Op> Result<OpResult<O>>.next(
    op: (OpResult<O>) -> Result<OpResult<P>>
): Result<OpResult<P>> =
    getOrNull()?.let { op(it) } ?: Result.failure(exceptionOrNull()!!)


// нужен список аргументов операций
// На входе: список операций
// Явный кастинг, т.к. возвращаемый тип функции должен быть списком с общей абстракцией, а каждая
// операция возвращает конкретный тип подмножества Op.
/**
 * Распараллеливание процесса на задачи
 * */
public suspend inline fun <O : Op> split(
    vararg opsBlock: suspend () -> Result<OpResult<O>>
): List<Result<OpResult<O>>> = coroutineScope {
    opsBlock.map { op ->
        async { op() }
    }.awaitAll()
}

/**
 * Распараллеливание выполнения процесса на задачи до первой ошибки.
 *
 * При возврате ошибки одной из задач, выполнение всех задач прекращается и возвращается эта ошибка.
 * При успешном выполнении всех задач, возвращается список их результатов.
 *
 * @param O op
 * @param opsBlock список функций для запуска подпроцессов
 * @return [Result] в случае успешн
 * */
public suspend fun <O : Op> splitAndCancelFirst(
    vararg opsBlock: suspend () -> Result<OpResult<O>>
): Result<List<OpResult<O>>> = coroutineScope {
    // Выступает посредником между ??
    val channel = Channel<Result<OpResult<O>>>(opsBlock.size)
    val jobs = opsBlock.map { task ->
        launch { channel.send(task()) }
    }

    val results = mutableListOf<OpResult<O>>()
    var firstFailure: Throwable? = null

    repeat(opsBlock.size) {
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
