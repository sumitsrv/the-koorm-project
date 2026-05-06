import org.gradle.api.Project
import java.io.File
import java.util.Properties

/**
 * Detects whether an Android SDK is reachable from this build.
 *
 * Mirrors the original root-level detection in [build.gradle.kts]: checks
 * `ANDROID_SDK_ROOT` / `ANDROID_HOME` env vars and the `sdk.dir` entry in
 * `local.properties`. Used by convention plugins to skip Android-target
 * configuration on machines without an SDK installed.
 */
internal fun Project.isAndroidSdkAvailable(): Boolean {
    val envSdk = System.getenv("ANDROID_SDK_ROOT") ?: System.getenv("ANDROID_HOME")
    if (envSdk != null && File(envSdk).exists()) return true

    val localProps = rootProject.file("local.properties")
    if (localProps.exists()) {
        val props = Properties().apply { localProps.inputStream().use { load(it) } }
        val sdkDir = props.getProperty("sdk.dir")
        if (sdkDir != null && File(sdkDir).exists()) return true
    }
    return false
}
