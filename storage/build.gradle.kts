import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.test.report)
    alias(libs.plugins.kover)
    alias(libs.plugins.test.logger)
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
    showPassedStandardStreams = true
    showStandardStreams = true
    showSimpleNames = true
}

dependencies {
    api(project(":api"))
    api(libs.kotlinx.serialization.json)
    implementation(project(":slot"))

    testImplementation(project(":commons"))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotest)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.kotest.htmlreporter)
    testImplementation(libs.mockk)
}

kover {
    reports {
        filters {
            includes {
                packages("ru.it_arch.tools.samples.ribeye.storage")
            }
        }
        total {
            html {
                onCheck = true
            }
            verify {
                rule {
                    disabled = true
                    bound {
                        minValue = 1
                    }
                }
            }
        }
    }
}
