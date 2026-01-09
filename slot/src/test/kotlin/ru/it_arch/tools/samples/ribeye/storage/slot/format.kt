package ru.it_arch.tools.samples.ribeye.storage.slot

import ru.it_arch.tools.samples.ribeye.data.Macronutrients
import ru.it_arch.tools.samples.ribeye.data.Resource

fun Macronutrients.format(): String =
"""{
    "proteins": ${proteins.boxed},
    "fats": ${fats.boxed},
    "carbs": ${carbs.boxed},
    "calories": ${calories.boxed}
}"""

fun Resource.Meat.format(): String =
"""{
    "macronutrients": "${macronutrients.format()}",
    "quantity": ${quantity.boxed},
    "expiration": "${expiration.boxed}"
}"""

// Десериализация JSON «на коленке»
fun String.fetchWeightQuantity(): Long =
    """\"quantity\"\s*:\s*(\d+)""".toRegex().find(this)
        ?.destructured?.component1()!!.toLong()
