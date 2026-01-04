package ru.it_arch.tools.samples.ribeye.dsl

import ru.it_arch.k3dm.ValueObject

/**
 * Цена в минимальных единицах учета — копейки, центы, сатоши, внутренняя валюта.
 * */
public interface Price : ValueObject.Value<Long> {
    override fun validate() {
        require(boxed >= 0) { "Price must be >= 0" }
    }
}
