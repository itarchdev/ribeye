package ru.it_arch.tools.samples.ribeye.app

import kotlinx.coroutines.runBlocking
import ru.it_arch.tools.samples.ribeye.data.Resource
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
    ).let(::Storage)


    slot.pull<Resource.Meat>(400L.toWeight())


}
