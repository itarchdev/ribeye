import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.test.logger)
    alias(libs.plugins.test.report)
    alias(libs.plugins.kover)
}

kotlin {
    jvmToolchain(libs.versions.javaVersion.get().toInt())
    compilerOptions {
        apiVersion.set(KotlinVersion.KOTLIN_2_3)
        freeCompilerArgs = freeCompilerArgs.get() + "-Xcontext-parameters"
    }
    explicitApi()
}

dependencies {
    api(libs.k3dm)
    api(libs.kotlinx.serialization.json)
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.datetime)

    testImplementation(project(":commons"))
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotest)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.kotest.htmlreporter)
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
                packages("ru.it_arch.tools.samples.ribeye")
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
                        minValue = 0
                    }
                }
            }
        }
    }
}
