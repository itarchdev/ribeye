package ru.it_arch.tools.samples.ribeye.app

import ru.it_arch.tools.samples.ribeye.dsl.CookingProcess
import ru.it_arch.tools.samples.ribeye.Storage
import ru.it_arch.tools.samples.ribeye.dsl.ResourceState

fun main(array: Array<String>) {
    val storage: Storage? = null
    println("Hello ribeye")

    fun CookingProcess.run(storage: Storage): Result<ResourceState.Steak> {

    }

    interpreter.run(storage!!)
        .onSuccess { steak ->

        }
        .onFailure {  }
}
