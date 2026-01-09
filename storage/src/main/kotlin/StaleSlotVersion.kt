package ru.it_arch.tools.samples.ribeye.storage

/** Выкидывается при проверки версии слота в рамках оптимистической блокировки */
internal class StaleSlotVersion(message: String) : IllegalStateException(message)
