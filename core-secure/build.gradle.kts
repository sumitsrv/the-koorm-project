import com.android.build.gradle.LibraryExtension

plugins {
    id("koorm.kmp.library")
}

extensions.findByType<LibraryExtension>()?.apply {
    namespace = "org.koorm.ocpd.secure"
}
