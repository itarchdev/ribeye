package ru.it_arch.tools.samples.ribeye.app

import ru.it_arch.tools.samples.ribeye.ResourceRepository
import ru.it_arch.tools.samples.ribeye.data.Resource
import ru.it_arch.tools.samples.ribeye.dsl.CookingProcess
import ru.it_arch.tools.samples.ribeye.dsl.impl.cookingProcess
import ru.it_arch.tools.samples.ribeye.pull
import ru.it_arch.tools.samples.ribeye.storage.impl.toWeight

/* Это повар. ЧТО делать. Здесь задаются бизнес-правила.
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
