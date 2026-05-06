import com.android.build.gradle.LibraryExtension

plugins {
    id("koorm.kmp.library")
    id("koorm.kmp.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core-domain"))
        }
    }
}

extensions.findByType<LibraryExtension>()?.apply {
    namespace = "org.koorm.ocpd.ui"
}
