import com.android.build.api.dsl.ApplicationExtension

plugins {
    id("koorm.android.application")
}

extensions.configure<ApplicationExtension> {
    namespace = "org.koorm.ocpd"
    defaultConfig {
        applicationId = "org.koorm.ocpd"
        versionCode = 1
        versionName = "1.0.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation(project(":core-domain"))
    implementation(project(":core-secure"))
    implementation(project(":core-data"))
    implementation(project(":feature-audio"))
    implementation(project(":shared-ui"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
}
