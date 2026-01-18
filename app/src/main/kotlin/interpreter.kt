package ru.it_arch.tools.samples.ribeye.app

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
