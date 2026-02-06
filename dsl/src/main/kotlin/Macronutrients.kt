package ru.it_arch.tools.samples.ribeye

import ru.it_arch.k3dm.ValueObject

/**
 * Белки, жиры, углеводы, энергетическая ценность (КБЖУ).
 * */
@ConsistentCopyVisibility
public data class Macronutrients private constructor(
    public val proteins: Proteins,
    public val fats: Fats,
    public val carbs: Carbohydrates,
    public val calories: Kcal
): ValueObject.Data {

    init {
        validate()
    }

    override fun validate() {
        require((proteins.boxed + fats.boxed + carbs.boxed) <= 100.0) { "Sum of proteins, fats and carbohydrates must be <= 100" }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ValueObject.Data> fork(vararg args: Any?): T =
        Builder().apply {
            proteins = args[0] as Proteins
            fats = args[1] as Fats
            carbs = args[2] as Carbohydrates
            calories = args[3] as Kcal
        }.build() as T

    public class Builder {
        public var proteins: Proteins? = null
        public var fats: Fats? = null
        public var carbs: Carbohydrates? = null
        public var calories: Kcal? = null

        public fun build(): Macronutrients {
            requireNotNull(proteins) { "proteins must be set" }
            requireNotNull(fats) { "fats must be set" }
            requireNotNull(carbs) { "carbs must be set" }
            requireNotNull(calories) { "calories must be set" }

            return Macronutrients(proteins!!, fats!!, carbs!!, calories!!)
        }
    }

    public class DslBuilder {
        public var proteins: Double? = null
        public var fats: Double? = null
        public var carbs: Double? = null
        public var calories: Double? = null

        public fun build(): Macronutrients {
            requireNotNull(proteins) { "proteins must be set" }
            requireNotNull(fats) { "fats must be set" }
            requireNotNull(carbs) { "carbs must be set" }
            requireNotNull(calories) { "calories must be set" }

            return Macronutrients(
                Proteins(proteins!!),
                Fats(fats!!),
                Carbohydrates(carbs!!),
                Kcal(calories!!)
            )
        }
    }

    /**
     * Белки на 100 г
     * */
    @JvmInline
    public value class Proteins private constructor(
        override val boxed: Double
    ): ValueObject.Value<Double>, Comparable<Proteins> {

        init {
            validate()
        }

        override fun validate() {
            require(boxed >= 0.0) { "Protein must be >= 0" }
        }

        override fun compareTo(other: Proteins): Int =
            boxed.compareTo(other.boxed)

        public operator fun plus(other: Proteins): Proteins =
            apply(boxed + other.boxed)

        public operator fun minus(other: Proteins): Proteins =
            apply(boxed - other.boxed)

        public operator fun times(other: Proteins): Proteins =
            apply(boxed * other.boxed)

        public operator fun div(other: Proteins): Proteins =
            apply(boxed / other.boxed)

        @Suppress("UNCHECKED_CAST")
        override fun <T : ValueObject.Value<Double>> apply(boxed: Double): T =
            Proteins(boxed) as T

        override fun toString(): String =
            "%.1f".format(boxed)

        public companion object {
            public val DEFAULT: Proteins = Proteins(0.0)

            public operator fun invoke(value: Double): Proteins =
                Proteins(value)
        }
    }

    /**
     * Жиры на 100 г
     * */
    @JvmInline
    public value class Fats private constructor(
        override val boxed: Double
    ): ValueObject.Value<Double>, Comparable<Fats> {

        init {
            validate()
        }

        override fun validate() {
            require(boxed >= 0.0) { "Fat must be >= 0" }
        }

        override fun compareTo(other: Fats): Int =
            boxed.compareTo(other.boxed)

        public operator fun plus(other: Fats): Fats =
            apply(boxed + other.boxed)

        public operator fun minus(other: Fats): Fats =
            apply(boxed - other.boxed)

        public operator fun times(other: Fats): Fats =
            apply(boxed * other.boxed)

        public operator fun div(other: Fats): Fats =
            apply(boxed / other.boxed)

        @Suppress("UNCHECKED_CAST")
        override fun <T : ValueObject.Value<Double>> apply(boxed: Double): T =
            Fats(boxed) as T

        override fun toString(): String =
            "%.1f".format(boxed)

        public companion object {
            public val DEFAULT:     Fats = Fats(0.0)

            public operator fun invoke(value: Double): Fats =
                Fats(value)
        }
    }

    /**
     * Углеводы на 100 г
     * */
    @JvmInline
    public value class Carbohydrates private constructor(
        override val boxed: Double
    ): ValueObject.Value<Double>, Comparable<Carbohydrates> {

        init {
            validate()
        }

        override fun validate() {
            require(boxed >=  0.0) { "Carbohydrate must be >= 0" }
        }

        override fun compareTo(other: Carbohydrates): Int =
            boxed.compareTo(other.boxed)

        public operator fun plus(other: Carbohydrates): Carbohydrates =
            apply(boxed + other.boxed)

        public operator fun minus(other: Carbohydrates): Carbohydrates =
            apply(boxed - other.boxed)

        public operator fun times(other: Carbohydrates): Carbohydrates =
            apply(boxed * other.boxed)

        public operator fun div(other: Carbohydrates): Carbohydrates =
            apply(boxed / other.boxed)

        @Suppress("UNCHECKED_CAST")
        override fun <T : ValueObject.Value<Double>> apply(boxed: Double): T =
            Carbohydrates(boxed) as T

        override fun toString(): String =
            "%.1f".format(boxed)

        public companion object {
            public val DEFAULT: Carbohydrates =
                Carbohydrates(0.0)

            public operator fun invoke(value: Double): Carbohydrates =
                Carbohydrates(value)
        }
    }

    /**
     * Килокалории
     * */
    @JvmInline
    public value class Kcal private constructor(
        override val boxed: Double
    ): ValueObject.Value<Double>, Comparable<Kcal> {

        init {
            validate()
        }

        override fun validate() {
            require(boxed >= 0.0) { "Kcal must be >= 0" }
        }

        override fun compareTo(other: Kcal): Int =
            boxed.compareTo(other.boxed)

        public operator fun plus(other: Kcal): Kcal =
            apply(boxed + other.boxed)

        public operator fun minus(other: Kcal): Kcal =
            apply(boxed - other.boxed)

        public operator fun times(other: Kcal): Kcal =
            apply(boxed * other.boxed)

        public operator fun div(other: Kcal): Kcal =
            apply(boxed / other.boxed)

        @Suppress("UNCHECKED_CAST")
        override fun <T : ValueObject.Value<Double>> apply(boxed: Double): T =
            Kcal(boxed) as T

        override fun toString(): String =
            "%.1f".format(boxed)

        public companion object {
            public val DEFAULT: Kcal = Kcal(0.0)

            public operator fun invoke(value: Double): Kcal =
                Kcal(value)
        }
    }
}
