import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Configures a Kotlin Multiplatform library with desktop (JVM 17), iOS
 * (X64 / Arm64 / SimulatorArm64), and conditionally Android targets.
 *
 * Adds a shared `iosMain` source set merging the three iOS targets, and
 * declares the standard kotlinx (coroutines, datetime, serialization) deps
 * in `commonMain` so consumers don't have to.
 */
class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
            val androidAvailable = isAndroidSdkAvailable()

            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
                if (androidAvailable) apply("com.android.library")
            }

            extensions.configure<KotlinMultiplatformExtension> {
                jvm("desktop") {
                    compilations.all {
                        compileTaskProvider.configure {
                            compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
                        }
                    }
                }

                iosX64()
                iosArm64()
                iosSimulatorArm64()

                if (androidAvailable) {
                    androidTarget {
                        compilations.all {
                            compileTaskProvider.configure {
                                compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
                            }
                        }
                    }
                }

                applyDefaultHierarchyTemplate()

                sourceSets.commonMain.configure {
                    dependencies {
                        implementation(libs.findLibrary("kotlinx-coroutines-core").get())
                        implementation(libs.findLibrary("kotlinx-serialization-json").get())
                        implementation(libs.findLibrary("kotlinx-datetime").get())
                    }
                }

                sourceSets.commonTest.configure {
                    dependencies {
                        implementation(kotlin("test"))
                        implementation(libs.findLibrary("kotlinx-coroutines-test").get())
                    }
                }
            }

            if (androidAvailable) {
                extensions.configure<LibraryExtension> {
                    compileSdk = libs.findVersion("androidCompileSdk").get().requiredVersion.toInt()
                    defaultConfig {
                        minSdk = libs.findVersion("androidMinSdk").get().requiredVersion.toInt()
                    }
                    compileOptions {
                        sourceCompatibility = JavaVersion.VERSION_17
                        targetCompatibility = JavaVersion.VERSION_17
                    }
                }
            }
        }
    }
}
