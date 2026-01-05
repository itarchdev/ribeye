package ru.it_arch.tools.samples.ribeye.dsl.impl

import ru.it_arch.tools.samples.ribeye.dsl.CookingProcess

public inline fun cookingProcess(block: CookingProcessImpl.Builder.() -> Unit): CookingProcess =
    CookingProcessImpl.Builder().apply(block).build()

