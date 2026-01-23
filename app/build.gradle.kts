plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.anji4cp.musicplayer"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.anji4cp.musicplayer"
        minSdk = 24
        targetSdk = 36
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
}

dependencies {

    // === LEGACY MEDIA SESSION (PALING STABIL) ===
    implementation("androidx.legacy:legacy-support-v4:1.0.0")

    // === CORE ===
    implementation("androidx.core:core:1.12.0")

    // === ANDROIDX STANDARD ===
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // === NAVIGATION ===
    implementation("androidx.navigation:navigation-fragment:2.7.6")
    implementation("androidx.navigation:navigation-ui:2.7.6")

    // === TEST ===
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
