package ru.it_arch.tools.samples.ribeye.app

import kotlinx.coroutines.runBlocking
import ru.it_arch.tools.samples.ribeye.Messenger
import ru.it_arch.tools.samples.ribeye.ResourceRepository
import ru.it_arch.tools.samples.ribeye.WriteResourceRepository
import ru.it_arch.tools.samples.ribeye.data.Resource
import ru.it_arch.tools.samples.ribeye.dsl.CookingProcess
import ru.it_arch.tools.samples.ribeye.dsl.Op
import ru.it_arch.tools.samples.ribeye.dsl.OpResult
import ru.it_arch.tools.samples.ribeye.dsl.Steak
import ru.it_arch.tools.samples.ribeye.dsl.next
import ru.it_arch.tools.samples.ribeye.dsl.split
import ru.it_arch.tools.samples.ribeye.dsl.splitAndCancelFirst
import ru.it_arch.tools.samples.ribeye.pull
import ru.it_arch.tools.samples.ribeye.storage.Storage
import ru.it_arch.tools.samples.ribeye.storage.impl.slotFactory
import ru.it_arch.tools.samples.ribeye.storage.impl.toPiece
import ru.it_arch.tools.samples.ribeye.storage.impl.toWeight

fun main(array: Array<String>) = runBlocking {
    println("Hello ribeye")

    // Создаем и заполняем хранилище
    val slot = slotFactory(
        10.toPiece(),
        10_000L.toWeight(),
        1_000L.toWeight(),
        5.toPiece()
    ).let(:: Storage)


    slot.pull<Resource.Meat>(400L.toWeight())


    /**
     * Use Case. Логика работы. "ЧТО" делать.
     * Смысл в том, чтобы написать логику используя лишь абстракции.
     *
     * 1. Подготовка мяса.
     *   1.1. Получение мяса.
     *   1.2. Проверка качества мяса.
     *   1.3. Маринование мяса.
     * 2. Подготовка гриля.
     *   2.1. Получение необходимого для розжига гриля (уголь, мангал и т.п.).
     *   2.2. Проверка состояния гриля.
     * 3. Стейк, соус и розмарин.
     *   3.1. Жарка стейка.
     *   3.2. Приготовление соуса.
     *     3.2.1. Получение компонентов для соуса.
     *     3.2.2. Приготовление соуса.
     *   3.3. Приготовление розмарина.
     *     3.3.1. Получение розмарина.
     *     3.3.2. Поджарка розмарина.
     * 4. Сервировка стейка.
     *
     * @param repository хранилище для получения ресурсов из внешней среды
     * @param messenger месенджер для отправки сообщений во внешнюю среду
     * */
    suspend fun CookingProcess.script(
        repository: ResourceRepository,
        messenger: Messenger
    ): Result<Steak> =
        // Разветвление на подготовку мяса и гриля
        splitAndCancelFirst(
            {
                // 1. Цепочка последовательных операций для мяса
                `get meat from storage`(repository) next // 1.1.
                    { `check meat freshness`(it) } next    // 1.2.
                    { `marinate meat`(it) }                // 1.3.
            },
            {
                // 2. Цепочка последовательных операций для гриля
                `get grill from storage`(repository) next // 2.1.
                    { `check grill`(it) }                    // 2.2.
            }
        ).map { (meat, grill) ->
            // 3. Разветвление на соус, специи и жарку мяса. Получение результата при любом исходе.
            @Suppress("UNCHECKED_CAST")
            split(
                { `roast meat`(meat as OpResult<Op.Meat.Marinate>, grill as OpResult<Op.Grill.Check>) }, // 3.1.
                {
                    `get sauce ingredients from storage`(repository) next // 3.2.1.
                        { `prepare sauce`(it) }                            // 3.2.2.
                },
                {
                    `get rosemary from storage`(repository) next // 3.3.1.
                        { `roast rosemary`(it) }                // 3.3.2.
                }
            )
        }.getOrNull()!!.let { (meatResult, sauceResult, rosemaryResult) ->
            /* На предыдущем шаге получены результаты 3-х операций:
            3.1. жарка стейка,
            3.2. приготовление соуса,
            3.3. поджарка розмарина.

            Жарка стейка — операция критическая, по этому ее неудача должна повлечь остановку
            всего процесса. */
            if (meatResult.isFailure) Result.failure(meatResult.exceptionOrNull()!!)
            else {
                /* Подготовка соуса и розмарина — некритические операции. В случае их неудачи
                процесс не прерывается, а выдается сообщение. */
                sauceResult.onFailure {
                    messenger.send("Something went wrong with the sauce: ${it.message}. Your steak will be fine without it.")
                }
                rosemaryResult.onFailure {
                    messenger.send("Something went wrong with the rosemary: ${it.message}. Your steak will be without it.")
                }

                /* 4. В завершении формируется стейк */
                @Suppress("UNCHECKED_CAST")
                `serve ribeye steak`(
                    meatResult.getOrNull() as OpResult<Op.Meat.Roast>,
                    sauceResult.getOrNull() as OpResult<Op.Sauce.Prepare>?,
                    rosemaryResult.getOrNull() as OpResult<Op.Rosemary.Roast>?
                )
            }
        }
}

