plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp) // For KSP
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0"
}

android {
    namespace = "no.uio.ifi.in2000.vrapp"
    compileSdk = 35

    // Exclude duplicate license/notice files from dependencies to avoid build conflicts.
    // These files are just metadata, they do not affect app functionality.
    packaging {
        resources {
            excludes += setOf(
                "META-INF/LICENSE.md",
                "META-INF/LICENSE",
                "META-INF/LICENSE-notice.md",
                "META-INF/NOTICE"
            )
        }
    }

    defaultConfig {
        applicationId = "no.uio.ifi.in2000.vrapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    testOptions {
        unitTests.all {
            it.jvmArgs("-XX:+EnableDynamicAgentLoading")
        }
    }

}

dependencies {
    implementation(libs.android.sdk) //Maplibre
    implementation(libs.android.plugin.annotation.v9)   //For Maplibre maptags

    implementation(libs.play.services.location) //Google Play Service Location
    implementation(libs.accompanist.permissions) //Android Permissions

    implementation(libs.androidx.material.icons.extended)

    implementation(libs.slf4j.android)  //SLF4J Logging

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.common.android)
    implementation(libs.androidx.navigation.runtime.android)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.monitor)
    implementation(libs.androidx.espresso.core)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.androidx.palette.ktx)
    implementation(libs.androidx.datastore.core.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


    implementation(libs.retrofit)
    implementation(libs.converter.gson) // For JSON parsing
    implementation(libs.logging.interceptor) // For logging requests
    implementation(libs.androidx.lifecycle.viewmodel.compose) // ViewModel for Jetpack Compose
    implementation(libs.androidx.lifecycle.runtime.ktx.v270) // LiveData support
    implementation(libs.androidx.lifecycle.viewmodel.ktx) // ViewModel with Kotlin Coroutines
    implementation(libs.retrofit2.kotlinx.serialization.converter)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)  // Use ksp for code generation


    // Jetpack Compose integration
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.lifecycle.viewmodel.savedstate) // SavedState

    // Views/Fragments integration
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)

    // Feature module support for Fragments
    implementation(libs.androidx.navigation.dynamic.features.fragment)

    // Testing Navigation
    androidTestImplementation(libs.androidx.navigation.testing)

    // JSON serialization library, works with the Kotlin serialization plugin
    implementation(libs.kotlinx.serialization.json.v173)

    // for async api-calls
    implementation(libs.kotlinx.coroutines.android)

    // ktor
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.content.negotiation)

    implementation(libs.androidx.datastore.preferences)



    implementation(libs.androidx.lifecycle.runtime.compose)

    // For mocking in Kotlin
    testImplementation(libs.mockk)
    androidTestImplementation(libs.mockk.android)

    testImplementation(libs.robolectric)


// For coroutine testing
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation(libs.robolectric)

// For assertions
    testImplementation("junit:junit:4.13.2")

}
