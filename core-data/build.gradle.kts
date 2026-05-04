import com.android.build.gradle.LibraryExtension

plugins {
    id("koorm.kmp.library")
    alias(libs.plugins.sqldelight)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core-domain"))
            api(project(":core-secure"))
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
        }
        // Android source set only exists when the convention plugin found an
        // SDK and added the android target — guard so the project still
        // configures cleanly on Android-less build machines.
        findByName("androidMain")?.dependencies {
            implementation(libs.sqldelight.driver.android)
            implementation(libs.sqlcipher.android)
            implementation(libs.androidx.core.ktx)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.driver.native)
        }
        getByName("desktopMain").dependencies {
            implementation(libs.sqldelight.driver.sqlite)
        }
    }
}

sqldelight {
    databases {
        create("KoormDatabase") {
            packageName.set("org.koorm.ocpd.data.db")
            // Use FTS / journal-mode defaults; the driver factory installs the
            // SQLCipher key (Android) or PRAGMA key (iOS) right after open.
        }
    }
}

extensions.findByType<LibraryExtension>()?.apply {
    namespace = "org.koorm.ocpd.data"
}
