import com.android.build.gradle.LibraryExtension

plugins {
    id("koorm.kmp.library")
    alias(libs.plugins.kotlin.serialization)
}

// Convention plugin already provides kotlinx coroutines/serialization/datetime
// in commonMain. No module-specific deps needed at this stage.

// Android namespace — applied only if the Android target is active.
extensions.findByType<LibraryExtension>()?.apply {
    namespace = "org.koorm.ocpd.core"
}
