import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * Configures an Android application module — `com.android.application` +
 * Kotlin/Android + Compose. Reads compileSdk/minSdk/targetSdk from the
 * version catalog. Skips silently if no SDK is reachable from the build
 * machine; the module simply won't build there.
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            if (!isAndroidSdkAvailable()) {
                logger.lifecycle("Android SDK not found; skipping ${path} configuration.")
                return
            }
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
                apply("org.jetbrains.compose")
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            extensions.configure<ApplicationExtension> {
                compileSdk = libs.findVersion("androidCompileSdk").get().requiredVersion.toInt()
                defaultConfig {
                    minSdk = libs.findVersion("androidMinSdk").get().requiredVersion.toInt()
                    targetSdk = libs.findVersion("androidTargetSdk").get().requiredVersion.toInt()
                }
                buildFeatures {
                    compose = true
                }
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
                packaging {
                    resources {
                        excludes += "/META-INF/{AL2.0,LGPL2.1}"
                    }
                }
            }

            extensions.configure<KotlinAndroidProjectExtension> {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                }
            }
        }
    }
}
