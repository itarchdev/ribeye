package ru.it_arch.tools.samples.ribeye.bl

import java.math.BigDecimal

public val Int.laborRate: LaborRate
    get() = LaborRate(toBigDecimal().setScale(LaborRate.SCALE))

internal val SECONDS_IN_HOUR = BigDecimal.valueOf(60*60)
