package ru.it_arch.tools.samples.ribeye


/**
 * Операции. На вход подаются результаты предыдущих операций, либо ресурсы из внешнего источника.
 * На выходе — результат этой операции. Для удобства операции сгруппированы по используемому ресурсу.
 * Операции должны быть функциональными интерфейсами.
 * */
public sealed interface Op {

    /** Операции с грилем */
    public sealed interface Grill : Op {
        /** Приемка необходимого для гриля. */
        public fun interface Get : Grill {
            public suspend operator fun invoke(storage: ResourceRepository): Result<State<Get>>
        }

        /** Розжиг гриля и приемка его готовности */
        public fun interface Check : Grill {
            /**
             * Операция розжига гриля и проверки готовности
             *
             * @param grill полученные компоненты гриля
             * @return [Result] результат подготовки гриля
             * */
            public suspend operator fun invoke(grill: State<Get>): Result<State<Check>>
        }
    }

    /** 3.2. */
    public sealed interface Sauce : Op {
        /** 3.2.1. */
        public fun interface Get : Sauce {
            public suspend operator fun invoke(storage: ResourceRepository): Result<State<Get>>
        }

        /** 3.2.2. */
        public fun interface Prepare : Sauce {
            public suspend operator fun invoke(sauce: State<Get>): Result<State<Prepare>>
        }
    }

    /** 3.3. */
    public sealed interface Rosemary : Op {
        /** 3.3.1 */
        public fun interface Get : Rosemary {
            public suspend operator fun invoke(storage: ResourceRepository): Result<State<Get>>
        }

        /** 3.3.2 */
        public fun interface Roast : Rosemary {
            public suspend operator fun invoke(rosemary: State<Get>): Result<State<Roast>>
        }
    }

    /** 1. Операции с мясом */
    public sealed interface Meat : Op {
        public fun interface Get : Meat {
            public suspend operator fun invoke(storage: ResourceRepository): Result<State<Get>>
        }

        public fun interface Check : Meat {
            public suspend operator fun invoke(meat: State<Get>): Result<State<Check>>
        }

        /** Маринование и мяса. */
        public fun interface Marinate : Meat {
            public suspend operator fun invoke(meat: State<Check>): Result<State<Marinate>>
        }

        public fun interface PrepareForRoasting : Meat {
            public suspend operator fun invoke(
                meat: State<Marinate>,
                grill: State<Grill.Check>
            ): Result<State<PrepareForRoasting>>
        }

        public fun interface Roast : Meat {
            public suspend operator fun invoke(steak: State<PrepareForRoasting>): Result<State<Roast>>
        }

        /** 4. */
        public fun interface Serve : Meat {
            public suspend operator fun invoke(
                steak: Result<State<Roast>>,
                sauce: Result<State<Sauce.Prepare>>,
                rosemary: Result<State<Rosemary.Roast>>
            ): Result<State<Serve>>
        }
    }
    public fun interface Finish : Op {
        public suspend operator fun invoke(steak: State<Meat.Serve>): Result<SteakReady>
    }
}
