package ru.it_arch.tools.samples.ribeye.app

import ru.it_arch.tools.samples.ribeye.ResourceRepository
import ru.it_arch.tools.samples.ribeye.dsl.CookingProcess
import ru.it_arch.tools.samples.ribeye.dsl.SteakReady
import ru.it_arch.tools.samples.ribeye.dsl.finish
import ru.it_arch.tools.samples.ribeye.dsl.next
import ru.it_arch.tools.samples.ribeye.dsl.`prepare sauce and rosemary`
import ru.it_arch.tools.samples.ribeye.dsl.`prepare meat and grll`

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
suspend fun CookingProcess.run(repository: ResourceRepository): Result<SteakReady> =
    `prepare meat and grll`(
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
