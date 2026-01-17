package ru.it_arch.tools.samples.ribeye.dsl

import ru.it_arch.tools.samples.ribeye.ResourceRepository
import ru.it_arch.tools.samples.ribeye.dsl.Op.Meat.Marinate

public sealed interface Op {
    public sealed interface Meat : Op {
        public fun interface Get : Meat {
            public suspend operator fun invoke(storage: ResourceRepository): Result<OpResult<Get>>
        }

        public fun interface Check : Meat {
            public suspend operator fun invoke(meat: OpResult<Get>): Result<OpResult<Check>>
        }

        public fun interface Marinate : Meat {
            public suspend operator fun invoke(meat: OpResult<Check>): Result<OpResult<Marinate>>
        }
    }

    public sealed interface Grill : Op {
        public fun interface Get : Grill {
            public suspend operator fun invoke(storage: ResourceRepository): Result<OpResult<Get>>
        }

        public fun interface Check : Grill {
            public suspend operator fun invoke(grill: OpResult<Get>): Result<OpResult<Check>>
        }
    }

    public fun interface Roast : Op {
        public suspend operator fun invoke(
            meat: OpResult<Marinate>,
            grill: OpResult<Grill.Check>
        ): Result<OpResult<Roast>>
    }

    public sealed interface Sauce : Op {
        public fun interface Get : Sauce {
            public suspend operator fun invoke(storage: ResourceRepository): Result<OpResult<Get>>
        }

        public fun interface Prepare : Sauce {
            public suspend operator fun invoke(sauce: OpResult<Get>): Result<OpResult<Prepare>>
        }
    }

    public sealed interface Rosemary : Op {
        public fun interface Get : Rosemary {
            public suspend operator fun invoke(storage: ResourceRepository): Result<OpResult<Get>>
        }

        public fun interface Roast : Rosemary {
            public suspend operator fun invoke(rosemary: OpResult<Get>): Result<OpResult<Roast>>
        }
    }

    public fun interface Finish : Op {
        public suspend operator fun invoke(
            meat: OpResult<Roast>,
            sauce: OpResult<Sauce.Prepare>?,
            rosemary: OpResult<Rosemary.Roast>?
        ): Result<Steak>
    }
}
