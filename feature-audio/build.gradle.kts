import com.android.build.gradle.LibraryExtension

plugins {
    id("koorm.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core-domain"))
        }
        findByName("androidMain")?.dependencies {
            implementation(libs.androidx.core.ktx)
        }
    }
}

extensions.findByType<LibraryExtension>()?.apply {
    namespace = "org.koorm.ocpd.audio"
}
