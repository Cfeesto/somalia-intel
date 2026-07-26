plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace         = "com.somalia.intel"
    compileSdk        = 35

    signingConfigs {
        create("release") {
            storeFile     = file("release.jks")
            storePassword = "somalia2024"
            keyAlias      = "somalia"
            keyPassword   = "somalia2024"
        }
    }

    defaultConfig {
        applicationId       = "com.somalia.intel"
        minSdk              = 26
        targetSdk           = 35
        versionCode         = 1
        versionName         = "1.0"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig   = signingConfigs.getByName("release")
        }
    }

    buildFeatures { compose = true }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.material3)
    implementation(libs.compose.navigation)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.coroutines)
}
