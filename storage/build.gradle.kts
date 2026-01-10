import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
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

tasks.withType<Test>().configureEach {
    useJUnitPlatform()

    testLogging {
        showStandardStreams = true
        showExceptions = true
        // disable reports. Max OS crash
        reports {
            html.required.set(false)
            junitXml.required.set(true)
        }
        events("passed", "skipped", "failed", "standard_out")
    }
}

dependencies {
    api(project(":api"))
    api(libs.kotlinx.serialization.json)
    implementation(project(":slot"))

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotest)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.kotest.htmlreporter)
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
