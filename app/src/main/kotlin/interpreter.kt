package ru.it_arch.tools.samples.ribeye.app

import kotlinx.coroutines.delay
import ru.it_arch.tools.samples.ribeye.bl.stateForGetMeat
import ru.it_arch.tools.samples.ribeye.bl.stateForCheckMeat
import ru.it_arch.tools.samples.ribeye.bl.stateForMarinate
import ru.it_arch.tools.samples.ribeye.Resource
import ru.it_arch.tools.samples.ribeye.Op
import ru.it_arch.tools.samples.ribeye.State
import ru.it_arch.tools.samples.ribeye.dsl.weight
import ru.it_arch.tools.samples.ribeye.dsl.valueChain
import kotlin.time.Duration.Companion.minutes
import kotlin.time.measureTime
import kotlin.time.measureTimedValue


/**
 * Каждая операция меняет состояние предмета операции — ресурса, изменяя его свойства: КБЖУ,
 * затраченное время, добавленную стоимость — согласно бизнес-логике.
 * */
val interpreter = cookingProcess {
    getMeat = { storage ->
        measureTimedValue { storage.pull<Resource.Meat>(350.weight) }.let { tv ->
            tv.value.mapCatching { meat ->
                meat.stateForGetMeat(tv.duration)
            }
        }
    }
    checkMeat = { meat ->
        // возможна дополнительная проверка с выкидыванием исключения
        measureTime { delay(1.minutes) }.let { elapsed ->
            runCatching { meat.stateForCheckMeat(elapsed) }
        }
    }
    marinate = { meat ->
        measureTime { delay(20.minutes) }.let { elapsed ->
            runCatching { meat.stateForMarinate(elapsed) }
        }
    }



    getGrill = { storage ->
        storage.pull<Resource.Grill>(800L.weight()).mapCatching { grill ->
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
            quantity = 0L.weight(), // все истрачено
            elapsed = 12.minutes,
            valueChain = 40.valueChain()
        ).let{ Result.success(it) }
    }

    steakStart = { meat, grill ->


    }

    getSauceIngredients = { storage ->
        storage.pull<Resource.SauceIngredients>(150L.weight()).mapCatching { sauce ->
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
