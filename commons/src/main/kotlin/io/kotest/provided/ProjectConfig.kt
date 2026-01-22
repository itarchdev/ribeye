package io.kotest.provided

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import io.kotest.core.test.TestScope
import io.kotest.extensions.htmlreporter.HtmlReporter

public object ProjectConfig : AbstractProjectConfig() {
    override val displayFullTestPath: Boolean = false
    override val extensions: List<HtmlReporter> = listOf(HtmlReporter(outputDir = "build/reports/kotest"))
    //override val logLevel = LogLevel.Info
}

/** Позитивный тест. Проверка того, что ожидается. Версия для корутин. */
public suspend fun FunSpecContainerScope.pos(name: String, test: suspend TestScope.() -> Unit) {
    test("🟢 $name", test)
}

/** Позитивный тест. Проверка того, что ожидается. */
public fun FunSpec.pos(name: String, test: suspend TestScope.() -> Unit) {
    test("🟢 $name", test)
}

/** Негативный тест. Проверка того, что не должно случиться. Версия для корутин. */
public suspend fun FunSpecContainerScope.neg(name: String, test: suspend TestScope.() -> Unit) {
    test("⛔️ $name", test)
}

/** Негативный тест. Проверка того, что не должно случиться. */
public fun FunSpec.neg(name: String, test: suspend TestScope.() -> Unit) {
    test("⛔️ $name", test)
}

/** Тест технического характера. Идейного смысла не имеет. Для обеспечения полного покрытия. Версия для корутин. */
public suspend fun FunSpecContainerScope.tech(name: String, test: suspend TestScope.() -> Unit) {
    test("🛠 $name", test)
}

/** Тест технического характера. Идейного смысла не имеет. Для обеспечения полного покрытия. */
public fun FunSpec.tech(name: String, test: suspend TestScope.() -> Unit) {
    test("🛠 $name", test)
}
