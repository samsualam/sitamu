import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")

    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use {
            load(it)
        }
    }
}

val supabaseUrl =
    localProperties.getProperty("SUPABASE_URL", "")

val supabasePublishableKey =
    localProperties.getProperty("SUPABASE_PUBLISHABLE_KEY", "")

android {
    namespace = "com.example.sitamu"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.sitamu"

        minSdk = 26
        targetSdk = 37

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "SUPABASE_URL",
            "\"$supabaseUrl\""
        )

        buildConfigField(
            "String",
            "SUPABASE_PUBLISHABLE_KEY",
            "\"$supabasePublishableKey\""
        )
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {

    // =========================
    // Jetpack Compose
    // =========================

    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)

    // =========================
    // AndroidX
    // =========================

    implementation(libs.androidx.core.ktx)

    implementation(
        libs.androidx.lifecycle.runtime.ktx
    )

    implementation(
        libs.androidx.lifecycle.viewmodel.compose
    )

    // =========================
    // Supabase
    // =========================

implementation(platform(libs.supabase.bom))
implementation(libs.supabase.postgrest)
implementation(libs.supabase.auth)
implementation(libs.supabase.functions)

    // =========================
    // Ktor HTTP Client
    // =========================

    implementation(
        libs.ktor.client.android
    )

    // =========================
    // Room Database
    // =========================

    implementation(
        libs.androidx.room.runtime
    )

    implementation(
        libs.androidx.room.ktx
    )

    ksp(
        libs.androidx.room.compiler
    )

    // =========================
    // Navigation
    // =========================

    implementation(
        libs.androidx.navigation.compose
    )

    // =========================
    // DataStore
    // =========================

    implementation(
        libs.androidx.datastore.preferences
    )

    // =========================
    // Coil
    // =========================

    implementation(
        libs.coil.compose
    )

    // =========================
    // Unit Tests
    // =========================

    testImplementation(
        libs.junit
    )

    testImplementation(
        "org.robolectric:robolectric:4.16.1"
    )

    testImplementation(
        platform(libs.androidx.compose.bom)
    )

    testImplementation(
        libs.androidx.compose.ui.test.junit4
    )

    // =========================
    // Android Tests
    // =========================

    androidTestImplementation(
        platform(libs.androidx.compose.bom)
    )

    androidTestImplementation(
        libs.androidx.compose.ui.test.junit4
    )

    androidTestImplementation(
        libs.androidx.espresso.core
    )

    androidTestImplementation(
        libs.androidx.junit
    )

    // =========================
    // Debug
    // =========================

    debugImplementation(
        libs.androidx.compose.ui.test.manifest
    )

    debugImplementation(
        libs.androidx.compose.ui.tooling
    )
}
