package ru.it_arch.tools.samples.ribeye.app

import ru.it_arch.tools.samples.ribeye.ResourceRepository
import ru.it_arch.tools.samples.ribeye.dsl.CookingProcess
import ru.it_arch.tools.samples.ribeye.dsl.SteakReady
import ru.it_arch.tools.samples.ribeye.dsl.finish
import ru.it_arch.tools.samples.ribeye.dsl.next
import ru.it_arch.tools.samples.ribeye.dsl.`prepare sauce and rosemary`
import ru.it_arch.tools.samples.ribeye.dsl.`prepare meat and grll`

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
        combine = `put meat on the grill`
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
