plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.progr3ssapp"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.progr3ssapp"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation("androidx.core:core-splashscreen:1.2.0")
    implementation("androidx.navigation:navigation-compose:2.8.8")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // LAB 6 - Backend Authentication

    // Retrofit - HTTP API kapcsolat
    implementation("com.squareup.retrofit2:retrofit:3.0.0")

    // Gson - JSON adatok feldolgozása
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")

    // OkHttp - hálózati naplózás
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ViewModel - bejelentkezési logika
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")

    // Lifecycle - Compose állapotkezelés
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")

    // Coroutines - aszinkron API hívások
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

}