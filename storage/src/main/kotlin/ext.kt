package ru.it_arch.tools.samples.ribeye.storage

import ru.it_arch.tools.samples.ribeye.data.Resource
import kotlin.reflect.KClass

public fun <T : Resource> notFound(type: KClass<out T>): Result<T> =
    Result.failure(StorageError("Resource ${type.simpleName} not found"))

internal fun emptySlot(): Result<String> =
    Result.failure((StorageError("Slot is empty")))

internal fun slotOverflow(): Result<Unit> =
    Result.failure((StorageError("Slot is full")))

/*
        data class SauceComponents(
            override val macronutrients: Macronutrients = macronutrients {
                proteins = 8.0
                fats = 0.5
                carbs = 7.5
                calories = 65.0
            }
        ) : Unpacked

        data class Rosemary(
            override val macronutrients: Macronutrients = macronutrients {
                proteins = 3.3
                fats = 5.9
                carbs = 20.7
                calories = 131.0
            }
        ) : Unpacked

* */
