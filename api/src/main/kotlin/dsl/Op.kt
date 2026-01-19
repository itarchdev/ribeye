package ru.it_arch.tools.samples.ribeye.dsl

import ru.it_arch.tools.samples.ribeye.ResourceRepository

public sealed interface Op {
    /** 1. Операции с мясом */
    public sealed interface Meat : Op {
        public fun interface Get : Meat {
            public suspend operator fun invoke(storage: ResourceRepository): Result<OpResult<Get>>
        }

        public fun interface Check : Meat {
            public suspend operator fun invoke(meat: OpResult<Get>): Result<OpResult<Check>>
        }

        /** Маринование и мяса. */
        public fun interface Marinate : Meat {
            public suspend operator fun invoke(meat: OpResult<Check>): Result<OpResult<Marinate>>
        }

        /** 3.1. */
        public fun interface Roast : Meat {
            public suspend operator fun invoke(
                meat: OpResult<Marinate>,
                grill: OpResult<Grill.Check>
            ): Result<OpResult<Roast>>
        }
    }

    /** Операции с грилем */
    public sealed interface Grill : Op {
        /** Получение необходимого для гриля. */
        public fun interface Get : Grill {
            public suspend operator fun invoke(storage: ResourceRepository): Result<OpResult<Get>>
        }

        /** Розжиг гриля */
        public fun interface Check : Grill {
            /**
             * Операция розжига гриля и проверки готовности
             *
             * @param grill полученные компоненты гриля
             * @return [Result] результат подготовки гриля
             * */
            public suspend operator fun invoke(grill: OpResult<Get>): Result<OpResult<Check>>
        }
    }

    /** 3.2. */
    public sealed interface Sauce : Op {
        /** 3.2.1. */
        public fun interface Get : Sauce {
            public suspend operator fun invoke(storage: ResourceRepository): Result<OpResult<Get>>
        }

        /** 3.2.2. */
        public fun interface Prepare : Sauce {
            public suspend operator fun invoke(sauce: OpResult<Get>): Result<OpResult<Prepare>>
        }
    }

    /** 3.3. */
    public sealed interface Rosemary : Op {
        /** 3.3.1 */
        public fun interface Get : Rosemary {
            public suspend operator fun invoke(storage: ResourceRepository): Result<OpResult<Get>>
        }

        /** 3.3.2 */
        public fun interface Roast : Rosemary {
            public suspend operator fun invoke(rosemary: OpResult<Get>): Result<OpResult<Roast>>
        }
    }

    /** 4. */
    public fun interface Finish : Op {
        public suspend operator fun invoke(
            meat: OpResult<Meat.Roast>,
            sauce: OpResult<Sauce.Prepare>?,
            rosemary: OpResult<Rosemary.Roast>?
        ): Result<Steak>
    }
}
