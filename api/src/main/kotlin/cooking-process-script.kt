package ru.it_arch.tools.samples.ribeye

import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
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
 * @param O op
 * @param opsBlock список функций для запуска подпроцессов
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
