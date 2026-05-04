import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("koorm.desktop.application")
}

dependencies {
    implementation(project(":core-domain"))
    implementation(project(":shared-ui"))
    implementation(compose.desktop.currentOs)
}

compose.desktop {
    application {
        mainClass = "org.koorm.ocpd.DesktopMainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "OCPDAssistant"
            packageVersion = "1.0.0"
        }
    }
}
