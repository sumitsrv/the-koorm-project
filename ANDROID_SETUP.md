# Android Setup Guide for OCPD Assistant

## To Add Android Support:

### 1. Install Android SDK
```bash
# Install Android Studio or just the SDK
# Download from: https://developer.android.com/studio
# Or use SDK command line tools
```

### 2. Set Environment Variable
```bash
# Add to your ~/.bashrc or ~/.zshrc
export ANDROID_HOME=/path/to/your/android/sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/tools
```

### 3. Update build.gradle.kts
Add these lines to the plugins block:
```kotlin
plugins {
    // ... existing plugins ...
    id("com.android.library") version "8.1.4"
}

kotlin {
    // Add Android target
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
    
    // ... existing targets ...
    
    sourceSets {
        // Add Android source set
        val androidMain by getting {
            dependencies {
                implementation("androidx.activity:activity-compose:1.8.2")
                implementation("androidx.appcompat:appcompat:1.6.1")
                implementation("androidx.core:core-ktx:1.12.0")
            }
        }
        // ... other source sets ...
    }
}

// Add Android configuration
android {
    namespace = "org.koorm.ocpd"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
}
```

### 4. Create Android App Module
Create a separate Android app module:
```kotlin
// In android-app/build.gradle.kts
plugins {
    id("com.android.application")
    kotlin("android")
}

dependencies {
    implementation(project(":shared"))
    implementation("androidx.activity:activity-compose:1.8.2")
}
```

### 5. Build Commands
```bash
# Build Android APK
./gradlew assembleDebug

# Install on device
./gradlew installDebug
```

## Current Status
- ✅ Core business logic is platform-agnostic
- ✅ File operations are ready for Android (AndroidFileOperations.kt exists)
- ✅ UI components use Compose Multiplatform
- ✅ All Android-specific code is already written

When you install the Android SDK, simply uncomment the Android configuration in build.gradle.kts and you'll be ready to build for Android!
