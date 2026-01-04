package ru.it_arch.tools.samples.ribeye.dsl.impl

import ru.it_arch.tools.samples.ribeye.dsl.CookingProcess
import ru.it_arch.tools.samples.ribeye.dsl.ResourceState

public inline fun cookingProcess(block: CookingProcessImpl.Builder.() -> Unit): CookingProcess =
    CookingProcessImpl.Builder().apply(block).build()

public inline fun grill(block: GrillImpl.): ResourceState.Grill =
