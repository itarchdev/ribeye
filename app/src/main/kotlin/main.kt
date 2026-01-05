package ru.it_arch.tools.samples.ribeye.app

import ru.it_arch.tools.samples.ribeye.dsl.CookingProcess
import ru.it_arch.tools.samples.ribeye.Storage

fun main(array: Array<String>) {
    val storage: Storage? = null
    println("Hello ribeye")

    fun CookingProcess.run(storage: Storage): Result<OpState.Steak> {

    }

    interpreter.run(storage!!)
        .onSuccess { steak ->

        }
        .onFailure {  }
}
