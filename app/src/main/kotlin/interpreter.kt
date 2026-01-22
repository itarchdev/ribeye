package ru.it_arch.tools.samples.ribeye.app

import ru.it_arch.tools.samples.ribeye.data.Resource
import ru.it_arch.tools.samples.ribeye.dsl.Op
import ru.it_arch.tools.samples.ribeye.dsl.State
import ru.it_arch.tools.samples.ribeye.dsl.impl.cookingProcess
import ru.it_arch.tools.samples.ribeye.dsl.impl.valueChain
import ru.it_arch.tools.samples.ribeye.pull
import ru.it_arch.tools.samples.ribeye.storage.impl.toWeight
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

val interpreter = cookingProcess {
    getMeat = { storage ->
        storage.pull<Resource.Meat>(350L.toWeight()).mapCatching { meat ->
            meat.takeUnless { it.isRotten() }?.let {
                // maybe not acceptable?

                State(
                    opType = Op.Meat.Get::class,
                    macronutrients = meat.macronutrients,
                    quantity = meat.quantity,
                    elapsed = 5.minutes,
                    valueChain = 10.valueChain()
                )
            } ?: throw RuntimeException("Meat is rotten")
        }
    }
}

/* Это повар. ЧТО делать. Здесь задаются бизнес-правила.
listener hear

val interpreter = cookingProcess {
    getMeat = { storage ->
        val meat = storage.pull<Resource.Meat>(350L.toWeight())
        Result.success()
    }
    listener = { event ->
        println(event)
    }
}*/


/*
*  Логика работы. Use case
fun ModuleProcess.run(
    module: Module,
    versionType: VersionType = VersionType.PATCH
): Result<OpResult.SemVer> = if (!module.changeVersionInCatalog.boxed) {
    `gradle clean build`(module) next
    { `publish artefact`(module, OpResult.SemVer("0.0.0")) }
} else {
    `gradle clean build`(module) next
    { `read gradle properties`(module) } next
    { `bump version`(module, it, versionType) } next
    { `write gradle properties`(module, it) } next
    { `publish artefact`(module, it) } next
    { `read version catalog`(module, it) } next
    { `replace version in version catalog`(module, it) } next
    { `write version catalog`(module, it) }
}

* */
