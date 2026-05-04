import java.io.File
import java.util.Properties

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "TheKoormProject"

include(":core-domain")
include(":shared-ui")
include(":app-desktop")

// Android module is conditional on SDK availability so the project still
// configures cleanly on machines without an Android SDK installed.
val androidSdkAvailable: Boolean = run {
    val env = System.getenv("ANDROID_SDK_ROOT") ?: System.getenv("ANDROID_HOME")
    if (env != null && File(env).exists()) return@run true
    val lp = File(rootDir, "local.properties")
    if (lp.exists()) {
        val props = Properties().apply { lp.inputStream().use { load(it) } }
        val sdkDir = props.getProperty("sdk.dir")
        if (sdkDir != null && File(sdkDir).exists()) return@run true
    }
    false
}

if (androidSdkAvailable) {
    include(":app-android")
} else {
    logger.lifecycle("Android SDK not found; :app-android will not be included in this build.")
}
