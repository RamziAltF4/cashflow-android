plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.ramzi.cashflow"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.ramzi.cashflow"
        minSdk = 26
        targetSdk = 37
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "NETLIFY_BASE_URL",
            "\"https://cashflowzi.netlify.app/.netlify/functions/\""
        )
    }

    // ✅ SIGNING CONFIG — untuk build release
    signingConfigs {
        create("release") {
            storeFile = file("../cashflow-release.jks")
            storePassword = "Cashflowbyzi820--"
            keyAlias = "cashflow"
            keyPassword = "Cashflowbyzi820--"
        }
    }

    buildTypes {
        release {
            // ✅ Pakai signing config di atas
            signingConfig = signingConfigs.getByName("release")

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
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    // ---- Compose ----
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.compose.material:material-icons-extended")
    implementation("dev.turingcomplete:kotlin-onetimepassword:2.4.0")

    // QR Code generator
    implementation("com.google.zxing:core:3.5.3")

    // ⭐ ProcessLifecycleOwner — untuk AppLockManager
    implementation("androidx.lifecycle:lifecycle-process:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.8.7")

    implementation("androidx.activity:activity-compose:1.10.1")

    // Biometric
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // ---- ViewModel / Navigation ----
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)

    // ---- DataStore ----
    implementation(libs.androidx.datastore.preferences)

    // ---- Firebase ----
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)

    // ---- WorkManager ----
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // ---- Coroutines ----
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // ---- Serialization ----
    implementation(libs.kotlinx.serialization.json)

    // Vico — Compose chart library
    implementation("com.patrykandpatrick.vico:compose:2.0.0-beta.2")
    implementation("com.patrykandpatrick.vico:compose-m3:2.0.0-beta.2")
    implementation("com.patrykandpatrick.vico:core:2.0.0-beta.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // ---- Retrofit + OkHttp ----
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.retrofit.kotlinx.serialization.converter)

    // ---- Test ----
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}