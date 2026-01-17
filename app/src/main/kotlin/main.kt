package ru.it_arch.tools.samples.ribeye.app

import kotlinx.coroutines.runBlocking
import ru.it_arch.tools.samples.ribeye.ResourceRepository
import ru.it_arch.tools.samples.ribeye.data.Resource
import ru.it_arch.tools.samples.ribeye.dsl.CookingProcess
import ru.it_arch.tools.samples.ribeye.dsl.next
import ru.it_arch.tools.samples.ribeye.dsl.split
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


    /** Use Case. Логика работы. "ЧТО" делать. Смысл в том, чтобы написать логику используя лишь абстракции.
     * 1.
     *
     * */
    suspend fun CookingProcess.script(repository: ResourceRepository) {
        val sp = split(
            {
                // цепочка последовательных операций для мяса
                `get meat from storage`(repository) next
                        { meatGet -> `check meat freshness`(meatGet) } next
                        { `marinate meat`(it) }
            },
            {
                // цепочка последовательных операций для гриля
                `get grill from storage`(repository) next
                        { `check grill`(it) }
            })


    }
}

