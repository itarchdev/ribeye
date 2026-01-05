package ru.it_arch.tools.samples.ribeye.data

import ru.it_arch.k3dm.Entity
import ru.it_arch.k3dm.ValueObject

public class ResourceEntity<T : Resource>(
    override val id: Id,
    override var content: T
) : Entity {

    override fun validate() {
    }

    public enum class Id : _Id {
        MEAT, GRILL, SAUCE, ROSEMARY
    }

    /** Технический промежуточный интерфейс для имплементации пустой валидации */
    private interface _Id : ValueObject.Sealed {
        override fun validate() {}
    }
}
