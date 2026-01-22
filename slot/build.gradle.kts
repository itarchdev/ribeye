import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.test.logger)
    alias(libs.plugins.test.report)
    alias(libs.plugins.kover)
}

dependencies {
    api(libs.kotlinx.coroutines.core)

    testImplementation(project(":api"))
    testImplementation(project(":commons"))
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotest)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.kotest.htmlreporter)
}

kotlin {
    jvmToolchain(libs.versions.javaVersion.get().toInt())
    compilerOptions {
        apiVersion.set(KotlinVersion.KOTLIN_2_3)
        freeCompilerArgs = freeCompilerArgs.get() + "-Xcontext-parameters"
    }
    explicitApi()
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()

    testLogging {
        showStandardStreams = false
        //showExceptions = true
        // disable reports. Max OS crash
        reports {
            html.required.set(false)
            junitXml.required.set(true)
        }
        //events("passed", "skipped", "failed", "standard_out")
        events.clear()
    }
}

testlogger {
    setTheme("mocha-parallel")
    showPassedStandardStreams = false
    showStandardStreams = false
    showSimpleNames = true
}

kover {
    reports {
        filters {
            includes {
                packages("ru.it_arch.tools.samples.ribeye.storage.slot")
            }
        }
        total {
            html {
                onCheck = true
            }
            verify {
                rule {
                    disabled = false
                    bound {
                        minValue = 1
                    }
                }
            }
        }
    }
}
