package ru.it_arch.tools.samples.ribeye.dsl

import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Функция-связка между последовательными операциями. Трансформация [State] результата одной
 * операции в результат выполнения другой.
 * */
public inline infix fun <O : Op, P : Op> Result<State<O>>.next(
    op: (State<O>) -> Result<State<P>>
): Result<State<P>> =
    getOrNull()?.let { op(it) } ?: Result.failure(exceptionOrNull()!!)

public inline infix fun Result<State<Op.Meat.Serve>>.finish(
    op: (State<Op.Meat.Serve>) -> Result<SteakReady>
): Result<SteakReady> =
    getOrNull()?.let { op(it) } ?: Result.failure(exceptionOrNull()!!)

/**
 * Распараллеливание процесса на задачи
 * */
public suspend fun `prepare sauce and rosemary`(
    meat: suspend () -> Result<State<Op.Meat.Roast>>,
    sauce: suspend () -> Result<State<Op.Sauce.Prepare>>,
    rosemary: suspend () -> Result<State<Op.Rosemary.Roast>>,
    serve: Op.Meat.Serve
): Result<State<Op.Meat.Serve>> = coroutineScope {
    val steakDeferred = async { meat() }
    val sauceDeferred = async { sauce() }
    val rosemary = async { rosemary() }
    serve(steakDeferred.await(), sauceDeferred.await(), rosemary.await())
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
public suspend fun `prepare meat and grll`(
    meat: suspend () -> Result<State<Op.Meat.Marinate>>,
    grill: suspend () -> Result<State<Op.Grill.Check>>,
    combine: Op.Meat.PrepareForRoasting
): Result<State<Op.Meat.PrepareForRoasting>> = coroutineScope {
    // Выступает посредником между ??
    val channel = Channel<Result<State<Op>>>(2)
    val meatJob = launch { channel.send(meat()) }
    val grillJob = launch { channel.send(grill()) }

    var meatResult: State<Op.Meat.Marinate>? = null
    var grillResult: State<Op.Grill.Check>? = null
    var firstFailure: Throwable? = null

    repeat(2) { i ->
        if (firstFailure != null) return@repeat
        channel.receive().fold(
            onSuccess = {
                @Suppress("UNCHECKED_CAST")
                when(i) {
                    0 -> meatResult = it as State<Op.Meat.Marinate>
                    1 -> grillResult = it as State<Op.Grill.Check>
                }
            },
            onFailure = { err ->
                firstFailure = err // Захват любой первой ошибки
                // отмена выполнения всех задач по отдельности
                meatJob.cancel()
                grillJob.cancel()
            }
        )
    }

    firstFailure?.let { Result.failure(it) } ?: combine(meatResult!!, grillResult!!)
}
