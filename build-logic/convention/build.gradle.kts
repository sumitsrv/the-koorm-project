plugins {
    `kotlin-dsl`
}

group = "org.koorm.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.compose.gradle.plugin)
    compileOnly(libs.kotlin.compose.compiler.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = "koorm.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("kmpCompose") {
            id = "koorm.kmp.compose"
            implementationClass = "KmpComposeConventionPlugin"
        }
        register("androidApplication") {
            id = "koorm.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("desktopApplication") {
            id = "koorm.desktop.application"
            implementationClass = "DesktopApplicationConventionPlugin"
        }
    }
}
