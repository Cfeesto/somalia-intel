plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace  = "com.somalia.intel"
    compileSdk = 35

    signingConfigs {
        create("release") {
            storeFile     = file("release.jks")
            storePassword = "somalia2024"
            keyAlias      = "somalia"
            keyPassword   = "somalia2024"
        }
    }

    defaultConfig {
        applicationId = "com.somalia.intel"
        minSdk        = 26
        targetSdk     = 35
        versionCode   = 1
        versionName   = "1.0"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled   = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        getByName("debug") {
            isMinifyEnabled = false
        }
    }

    buildFeatures { compose = true }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.compose)
    implementation(libs.androidx.activity.compose)
    // Compose BOM — pins all compose versions
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.navigation)
    // Networking
    implementation(libs.okhttp)
    // Async
    implementation(libs.kotlinx.coroutines)
    // JSON
    implementation(libs.kotlinx.serialization.json)
    // Room — offline article cache
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    // DataStore — persist API key + prefs
    implementation(libs.datastore.preferences)
    // WorkManager — background 15-min refresh
    implementation(libs.work.runtime.ktx)
    // Coil — article thumbnail images
    implementation(libs.coil.compose)
}
