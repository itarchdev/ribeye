package ru.it_arch.tools.samples.ribeye.app

import ru.it_arch.tools.samples.ribeye.data.Resource
import ru.it_arch.tools.samples.ribeye.dsl.Op
import ru.it_arch.tools.samples.ribeye.dsl.State
import ru.it_arch.tools.samples.ribeye.dsl.impl.cookingProcess
import ru.it_arch.tools.samples.ribeye.dsl.impl.valueChain
import ru.it_arch.tools.samples.ribeye.pull
import ru.it_arch.tools.samples.ribeye.storage.impl.toPiece
import ru.it_arch.tools.samples.ribeye.storage.impl.toWeight
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes


/**
 * Каждая операция меняет состояние предмета операции — ресурса, изменяя его свойства: КБЖУ,
 * затраченное время, добавленную стоимость — согласно бизнес-логике.
 * */
val interpreter = cookingProcess {
    getMeat = { storage ->
        storage.pull<Resource.Meat>(350L.toWeight()).mapCatching { meat ->
            meat.takeUnless { it.isRotten() }?.let {
                State(
                    opType = Op.Meat.Get::class,
                    macronutrients = meat.macronutrients,
                    quantity = meat.quantity,
                    elapsed = 2.minutes,
                    valueChain = 10.valueChain()
                )
            } ?: throw RuntimeException("Meat is rotten")
        }
    }
    checkMeat = { meat ->
        // возможна дополнительная проверка с выкидыванием исключения
        State(
            opType = Op.Meat.Check::class,
            macronutrients = meat.macronutrients,
            quantity = meat.quantity,
            elapsed = meat.elapsed + 1.minutes,
            valueChain = meat.valueChain + 20.valueChain()
        ).let{ Result.success(it) }
    }
    marinate = { meat ->
        State(
            opType = Op.Meat.Marinate::class,
            macronutrients = meat.macronutrients,
            quantity = meat.quantity,
            elapsed = meat.elapsed + 15.minutes,
            valueChain = meat.valueChain + 30.valueChain()
        ).let{ Result.success(it) }
    }
    getGrill = { storage ->
        storage.pull<Resource.Grill>(800L.toWeight()).mapCatching { grill ->
            State(
                opType = Op.Grill.Get::class,
                macronutrients = grill.macronutrients,
                quantity = grill.quantity,
                elapsed = 2.minutes,
                valueChain = 10.valueChain()
            )
        }
    }
    checkGrill = { grill ->
        // Моделируется процесс проверки готовности гриля
        State(
            opType = Op.Grill.Check::class,
            macronutrients = grill.macronutrients,
            quantity = 0L.toWeight(), // все истрачено
            elapsed = 12.minutes,
            valueChain = 40.valueChain()
        ).let{ Result.success(it) }
    }
    getSauceIngredients = { storage ->
        storage.pull<Resource.SauceIngredients>(150L.toWeight()).mapCatching { sauce ->
            State(
                opType = Op.Sauce.Get::class,
                macronutrients = sauce.macronutrients,
                quantity = sauce.quantity,
                elapsed = 2.minutes,
                valueChain = 10.valueChain()
            )
        }
    }
    prepareSauce = { sauce ->
        State(
            opType = Op.Sauce.Prepare::class,
            macronutrients = sauce.macronutrients,
            quantity = sauce.quantity,
            elapsed = 20.minutes,
            valueChain = 60.valueChain()
        ).let{ Result.success(it) }
    }
    getRosemary = { storage ->
        storage.pull<Resource.Rosemary>(3.toPiece()).mapCatching { rosemary ->
            State(
                opType = Op.Rosemary.Get::class,
                macronutrients = rosemary.macronutrients,
                quantity = rosemary.quantity,
                elapsed = 2.minutes,
                valueChain = 10.valueChain()
            )
        }
    }
    roastRosemary = { rosemary ->
        State(
            opType = Op.Rosemary.Roast::class,
            macronutrients = rosemary.macronutrients,
            quantity = rosemary.quantity,
            elapsed = 3.minutes,
            valueChain = 30.valueChain()
        ).let{ Result.success(it) }
    }
    steakStart = { meat, grill ->
        State(
            opType = Op.Meat.PrepareForRoasting::class,

        ).let{ Result.success(it) }
    }
}

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
