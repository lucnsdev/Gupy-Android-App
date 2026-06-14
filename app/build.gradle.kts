plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "lucns.gupy"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "lucns.gupy"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
}