package ru.it_arch.tools.samples.ribeye.dsl.impl

import ru.it_arch.tools.samples.ribeye.dsl.ResourceState
import ru.it_arch.tools.samples.ribeye.storage.ResourceOld

//public fun Resource.Meat.toInitState(): ResourceState.Meat =

public fun ResourceOld.Grill.toInitState(): ResourceState.Grill =
    GrillImpl.Builder().apply {
        macronutrients = this@toInitState.macronutrients
    }.build()
