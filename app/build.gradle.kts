plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Versi release di-override dari CI: -PrelVersionName=1.0.0 -PrelVersionCode=N
val relVersionName: String = (project.findProperty("relVersionName") as? String) ?: "1.0.0"
val relVersionCode: Int = (project.findProperty("relVersionCode") as? String)?.toIntOrNull() ?: 1

android {
    namespace = "com.gyosanila.kartcilik"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.gyosanila.logichild"
        minSdk = 23
        targetSdk = 35
        // Dev: versi tetap, gak naik tiap push (naik cuma pas tag release).
        versionCode = 1
        versionName = "1.0.0"

        // AdMob App ID asli (Logichild)
        manifestPlaceholders["ADMOB_APP_ID"] = "ca-app-pub-6023230476562279~4364973144"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        // Debug keystore fixed & di-commit supaya SHA-1 stabil antar build CI
        // (kalau nanti butuh Firebase / Google login).
        getByName("debug") {
            storeFile = file("keystore/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        create("release") {
            val ksPath = System.getenv("KEYSTORE_PATH")
            if (!ksPath.isNullOrEmpty() && file(ksPath).exists()) {
                storeFile = file(ksPath)
                storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
                keyAlias = System.getenv("KEY_ALIAS") ?: ""
                keyPassword = System.getenv("KEY_PASSWORD") ?: ""
            } else {
                storeFile = file("keystore/debug.keystore")
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
            }
        }
    }

    buildTypes {
        debug {
            // Tandai build debug: versi jadi "1.0.0-debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Versi AAB/release APK dari CI: versionName/versionCode di-override khusus release
    // (versionCode incremental otomatis per tag).
    androidComponents {
        onVariants(selector().withBuildType("release")) { variant ->
            variant.outputs.forEach { output ->
                output.versionName.set(relVersionName)
                output.versionCode.set(relVersionCode)
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.play.services.ads)
    implementation(libs.androidx.lifecycle.runtime.compose)
    debugImplementation(libs.androidx.ui.tooling)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
}
