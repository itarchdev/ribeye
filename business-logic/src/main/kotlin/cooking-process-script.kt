package ru.it_arch.tools.samples.ribeye.bl

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import ru.it_arch.tools.samples.ribeye.ResourceRepository
import ru.it_arch.tools.samples.ribeye.dsl.CookingProcess
import ru.it_arch.tools.samples.ribeye.dsl.Op
import ru.it_arch.tools.samples.ribeye.dsl.State
import ru.it_arch.tools.samples.ribeye.dsl.SteakReady

/**
 * Технологическая карта приготовления стейка.
 *
 * 1. Подготовка мяса и гриля.
 *   1.1. Мясо.
 *     1.1.1. Получение мяса.
 *     1.1.2. Проверка качества мяса.
 *     1.1.3. Маринование мяса.
 *   1.2. Гриль.
 *     1.2.1. Получение необходимого для розжига гриля (уголь, мангал и т.п.).
 *     1.2.2. Проверка состояния гриля.
 *
 * 2. Стейк, соус и розмарин.
 *   2.1. Жарка стейка.
 *   2.2. Приготовление соуса.
 *     2.2.1. Получение компонентов для соуса.
 *     2.2.2. Приготовление соуса.
 *   2.3. Приготовление розмарина.
 *     2.3.1. Получение розмарина.
 *     2.3.2. Поджарка розмарина.
 * 3. Сервировка стейка.
 * 4. Готовность стейка
 *
 * Процессы подготовки мяса и гриля проходят параллельно. При возникновении ошибки в одной из
 * операций, процессы прерываются. Процессы жарки мяса, приготовления соуса и розмарина также
 * проходят параллельно. При этом при возникновении ошибки в процессе приготовления мяса (критический
 * процесс), прерываются все остальные процессы. Ошибки в процессах приготовления соуса и розмарина
 * не влекут прерывания общего хода, а создаются сообщения, что итоговый стейк будет без них.
 *
 * @param repository хранилище для получения ресурсов из внешней среды
 * */
public suspend fun CookingProcess.run(repository: ResourceRepository): Result<SteakReady> =
    `prepare meat and grill`(
        meat = {
            `get meat from storage`(repository) next
                { `check meat freshness`(it) } next
                { `marinate meat`(it) }
        },
        grill = {
            `get grill from storage`(repository) next
                { `check grill`(it) }
        },
        combine = `put meat on the grill and start roasting`
    ) next {
        `prepare sauce and rosemary`(
            meat = { `roast meat`(it) },
            sauce = {
                `get sauce ingredients from storage`(repository) next
                    { `prepare sauce`(it) }
            },
            rosemary = {
                `get rosemary from storage`(repository) next
                    { `roast rosemary`(it) }
            },
            serve = `serve steak`
        )
    } finish { `final check and create ribeye steak`(it) }

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
 * Распараллеливание выполнения процесса на задачи до первой ошибки.
 *
 * При возврате ошибки одной из задач, выполнение всех задач прекращается и возвращается эта ошибка.
 * При успешном выполнении всех задач, возвращается список их результатов.
 *
 * @return [Result] в случае успешн
 * */
public suspend fun `prepare meat and grill`(
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

    repeat(2) {
        if (firstFailure != null) return@repeat
        channel.receive().fold(
            onSuccess = { result ->
                @Suppress("UNCHECKED_CAST")
                when(result.opType) {
                    Op.Meat.Marinate::class -> meatResult = result as State<Op.Meat.Marinate>
                    Op.Grill.Check::class -> grillResult = result as State<Op.Grill.Check>
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

/**
 * Параллельное выполнение жарки мяса, подготовки соуса и розмарина.
 * Жарка мяса — критическая операция, при ошибке которой остальные операции отменяються.
 * Подготовка соуса и розмарина — операции некритические. При их сбое общий процесс не отменяется.
 * */
public suspend fun `prepare sauce and rosemary`(
    meat: suspend () -> Result<State<Op.Meat.Roast>>,
    sauce: suspend () -> Result<State<Op.Sauce.Prepare>>,
    rosemary: suspend () -> Result<State<Op.Rosemary.Roast>>,
    serve: Op.Meat.Serve
): Result<State<Op.Meat.Serve>> = coroutineScope {
    val channel = Channel<ResultWrapper>(3)
    launch { channel.send(ResultWrapper.Meat(meat())) }
    val sauceJob = launch { channel.send(ResultWrapper.Sauce(sauce())) }
    val rosemaryJob = launch { channel.send(ResultWrapper.Rosemary(rosemary())) }

    var meatResult: Result<State<Op.Meat.Roast>>? = null
    var sauceResult: Result<State<Op.Sauce.Prepare>>? = null
    var rosemaryResult: Result<State<Op.Rosemary.Roast>>? = null

    repeat(3) {
        when (val received = channel.receive()) {
            is ResultWrapper.Meat -> {
                meatResult = received.result
                if (meatResult.isFailure) {
                    sauceJob.cancel()
                    rosemaryJob.cancel()
                    return@coroutineScope Result.failure(meatResult.exceptionOrNull()!!)
                }
            }
            is ResultWrapper.Sauce -> sauceResult = received.result
            is ResultWrapper.Rosemary -> rosemaryResult = received.result
        }
    }
    serve(meatResult!!, sauceResult!!, rosemaryResult!!)
}

/**
 * Вспомогательная обертка для [Result] для обхода стирания типов.
 * */
private sealed interface ResultWrapper {
    data class Meat(val result: Result<State<Op.Meat.Roast>>) : ResultWrapper
    data class Sauce(val result: Result<State<Op.Sauce.Prepare>>) : ResultWrapper
    data class Rosemary(val result: Result<State<Op.Rosemary.Roast>>) : ResultWrapper
}
