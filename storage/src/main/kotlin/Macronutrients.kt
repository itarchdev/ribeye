package ru.it_arch.tools.samples.ribeye.storage

import ru.it_arch.k3dm.ValueObject

/**
 * Белки, жиры, углеводы, энергетическая ценность (КБЖУ).
 * */
public interface Macronutrients : ValueObject.Data {
    public val proteins: Proteins
    public val fats: Fats
    public val carbs: Carbohydrates
    public val calories: Kcal

    override fun validate() {
        require((proteins.boxed + fats.boxed + carbs.boxed) <= 100.0) { "Sum of proteins, fats and carbohydrates must be <= 100" }
    }

    /**
     * Белки на 100 г
     * */
    public interface Proteins : ValueObject.Value<Double>, Comparable<Proteins> {
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
    }

    /**
     * Жиры на 100 г
     * */
    public interface Fats : ValueObject.Value<Double>, Comparable<Fats> {
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
    }

    /**
     * Углеводы на 100 г
     * */
    public interface Carbohydrates : ValueObject.Value<Double>, Comparable<Carbohydrates> {
        override fun validate() {
            require(boxed >= 0.0) { "Carbohydrate must be >= 0" }
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
    }

    /**
     * Килокалории
     * */
    public interface Kcal : ValueObject.Value<Double>, Comparable<Kcal> {
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
    }
}
