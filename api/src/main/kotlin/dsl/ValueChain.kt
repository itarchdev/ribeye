package ru.it_arch.tools.samples.ribeye.dsl

import ru.it_arch.k3dm.ValueObject

/**
 * Добавленная стоимость в минимальных единицах учета — копейки, центы, сатоши, внутренняя валюта.
 * */
public interface ValueChain : ValueObject.Value<Long> {
    override fun validate() {
        require(boxed >= 0) { "Value Chain must be >= 0" }
    }
}
