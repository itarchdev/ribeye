package ru.it_arch.tools.samples.ribeye.dsl.impl

import ru.it_arch.tools.samples.ribeye.dsl.CookingProcess
import ru.it_arch.tools.samples.ribeye.dsl.ValueChain

public inline fun cookingProcess(block: CookingProcessImpl.Builder.() -> Unit): CookingProcess =
    CookingProcessImpl.Builder().apply(block).build()

public fun Long.valueChain(): ValueChain =
    ValueChainImpl(this)

public fun Int.valueChain(): ValueChain =
    ValueChainImpl(toLong())
