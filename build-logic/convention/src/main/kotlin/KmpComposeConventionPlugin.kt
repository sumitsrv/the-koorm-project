import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Adds Compose Multiplatform (runtime/foundation/material3/ui) to a KMP
 * library that already applies [KmpLibraryConventionPlugin]. Wires the
 * Kotlin 2.0 Compose Compiler plugin instead of the legacy
 * `kotlinCompilerExtensionVersion` Android DSL.
 */
class KmpComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.compose")
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            val compose = extensions.getByType<ComposeExtension>().dependencies
            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.commonMain.configure {
                    dependencies {
                        implementation(compose.runtime)
                        implementation(compose.foundation)
                        implementation(compose.material3)
                        @Suppress("UnstableApiUsage")
                        implementation(compose.components.resources)
                    }
                }
            }
        }
    }
}
