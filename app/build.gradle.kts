plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.spotless)

    alias(libs.plugins.googleServices)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "com.rpn.mosquetime"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.rpn.mosquetime"
        minSdk = 23
        targetSdk = 36

        versionCode = 3
        versionName = "2.0.0-MVI"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += listOf(
            "-Xopt-in=androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
            "-Xopt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**/*.kt")
        licenseHeaderFile(rootProject.file("$rootDir/spotless/copyright.kt"))

        ktlint("1.6.0")
            .customRuleSets(
                listOf(
                    "io.nlopez.compose.rules:ktlint:0.4.16"
                )
            )
            .editorConfigOverride(mapOf("disabled_rules" to "compose:modifier-missing-check"))
        trimTrailingWhitespace()
        endWithNewline()
    }
    format("kts") {
        target("**/*.kts")
        targetExclude("**/build/**/*.kts")
        // Look for the first line that doesn't have a block comment (assumed to be the license)
        licenseHeaderFile(rootProject.file("spotless/copyright.kts"), "(^(?![\\/ ]\\*).*$)")
    }
    format("xml") {
        target("**/*.xml")
        targetExclude("**/build/**/*.xml")
        // Look for the first XML tag that isn't a comment (<!--) or the xml declaration (<?xml)
        licenseHeaderFile(rootProject.file("spotless/copyright.xml"), "(<[^!?])")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.animation.graphics)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.windowsize)

    // Permissions
    implementation(libs.accompanist.permissions)

    // Hilt dependencies
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3.android)
    ksp(libs.hilt.compiler)

    // Navigation Compose
//    implementation(libs.androidx.material3.navigation3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
//    implementation(libs.androidx.compose.material3.adaptive)
//    implementation(libs.androidx.compose.material3.adaptive.layout)
//    implementation(libs.androidx.compose.material3.adaptive.navigation)

    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)



    // Dependency Injection (Koin)
    // Lightweight dependency injection framework for Kotlin and Compose
    implementation(libs.bundles.koin)
    api(libs.koin.core)

    // Networking (Ktor)
    // Asynchronous HTTP client for network requests and JSON serialization
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.okhttp)

    // Database (Room)
    // Local SQL database with Kotlin extensions for persistence
    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)

    // DataStore
    // Key-value and protocol buffer storage for preferences and structured data
    implementation(libs.bundles.datastore)

    // Media (Media3/ExoPlayer)
    // Media playback components for audio and video in a music player app
    implementation(libs.bundles.media3)

    // SplashScreen
    implementation(libs.androidx.core.splashscreen)

    // Material components optimized for TV apps
    implementation(libs.androidx.tv.material)

    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)
    implementation("com.github.msarhan:ummalqura-calendar:2.0.2")
    implementation("com.google.code.gson:gson:2.13.1")
    implementation ("com.intuit.sdp:sdp-android:1.1.1")
    implementation ("com.intuit.ssp:ssp-android:1.1.1")



    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
