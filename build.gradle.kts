import java.io.File
import java.util.Properties
import com.android.build.api.dsl.ApplicationExtension

plugins {
    kotlin("multiplatform") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    id("org.jetbrains.compose") version "1.6.0"
    // Apply Android plugin only when SDK is available
    id("com.android.application") version "8.12.2" apply false
    id("maven-publish")
}

group = "org.koorm"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    gradlePluginPortal()
}

// Detect whether an Android SDK is available; if not, skip applying the Android plugin/targets
val enableAndroid: Boolean by lazy {
    val envSdk = System.getenv("ANDROID_SDK_ROOT") ?: System.getenv("ANDROID_HOME")
    if (envSdk != null && File(envSdk).exists()) return@lazy true

    val lp = rootProject.file("local.properties")
    if (lp.exists()) {
        val props = Properties().apply { lp.inputStream().use { load(it) } }
        val sdkDir = props.getProperty("sdk.dir")
        if (sdkDir != null && File(sdkDir).exists()) return@lazy true
    }
    false
}

if (enableAndroid) {
    apply(plugin = "com.android.application")
} else {
    logger.lifecycle("Android SDK not found; skipping Android plugin and target configuration.")
}

kotlin {
    // JVM Desktop
    jvm("desktop") {
        jvmToolchain(17)
    }

    // JavaScript/Browser
    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
        binaries.executable()
    }

    // Android targets (only if SDK is present)
    if (enableAndroid) {
        androidTarget {
            compilations.all {
                kotlinOptions {
                    jvmTarget = "17" // was 1.8 causing mismatch with Java 17
                }
            }
        }
    }

    // iOS targets (can be built on macOS with Xcode)
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        // Common source set for all platforms
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")

                // Compose Multiplatform
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
            }
        }

        // iOS
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }

        // JVM Desktop
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }

        // JavaScript/Browser
        val jsMain by getting {
            dependencies {
                implementation(compose.ui)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.runtime)
            }
        }

        // Android (only if SDK is present)
        if (enableAndroid) {
            val androidMain by getting {
                dependencies {
                    implementation("androidx.activity:activity-compose:1.9.0")
                }
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "org.koorm.ocpd.DesktopMainKt"

        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb
            )
            packageName = "OCPD Assistant"
            packageVersion = "1.0.0"
            description = "Your Compassionate Productivity Companion"
            copyright = "© 2025 OCPD Assistant"
            vendor = "Koorm"
        }
    }
}

// Configure Android only when SDK is present
if (enableAndroid) {
    extensions.configure<ApplicationExtension>("android") {
        namespace = "org.koorm.ocpd"
        compileSdk = 34
        defaultConfig {
            applicationId = "org.koorm.ocpd"
            minSdk = 24
            targetSdk = 34
            versionCode = 1
            versionName = "1.0"
        }
        buildTypes {
            maybeCreate("debug")
            maybeCreate("release")
        }
        buildFeatures {
            compose = true
        }
        composeOptions { // added to align Compose Compiler with Kotlin 1.9.22
            kotlinCompilerExtensionVersion = "1.5.11"
        }
        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }

    // Convenience task to install & launch on a connected device/emulator
    tasks.register("runAndroid") {
        group = "application"
        description = "Install and launch the Android debug app on a connected device/emulator"
        dependsOn("installDebug")
        doLast {
            val adbCandidate = sequenceOf(
                System.getenv("ADB"),
                System.getenv("ANDROID_HOME")?.let { "$it/platform-tools/adb" },
                System.getenv("ANDROID_SDK_ROOT")?.let { "$it/platform-tools/adb" },
                "adb"
            ).firstOrNull { it != null } ?: "adb"
            val activity = "org.koorm.ocpd/.MainActivity"
            println("Attempting to start $activity using $adbCandidate")
            try {
                project.exec { commandLine(adbCandidate, "shell", "am", "start", "-n", activity) }
            } catch (e: Exception) {
                println("Could not launch activity: ${e.message}")
                println("If no devices are connected, start an emulator or plug in a device and run: $adbCandidate devices")
                println("You can manually start the app from the launcher after installation.")
            }
        }
    }
}

publishing {
    publications {
        withType<org.gradle.api.publish.maven.MavenPublication>().configureEach {
            pom {
                licenses {
                    license {
                        name.set("Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International")
                        url.set("https://creativecommons.org/licenses/by-nc-sa/4.0/")
                        distribution.set("repo")
                    }
                }
            }
        }
    }
}

// Custom Desktop run task because default desktopRun isn't picking up mainClass
afterEvaluate {
    val desktopRuntime = configurations.findByName("desktopRuntimeClasspath")
    if (desktopRuntime != null) {
        tasks.register<JavaExec>("runDesktop") {
            group = "application"
            description = "Run the Compose Desktop application"
            mainClass.set("org.koorm.ocpd.DesktopMainKt")
            classpath = files(tasks.named("desktopJar"), desktopRuntime)
            dependsOn("desktopJar")
        }
    } else {
        logger.lifecycle("desktopRuntimeClasspath configuration not found; skipping runDesktop task creation")
    }
}
