plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.dawit.androidapp4"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.dawit.androidapp4"
        minSdk = 25
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    // RecyclerView
    implementation(libs.androidx.recyclerview)

    // Lifecycle and ViewModel
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Retrofit and JSON
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.gson)

    // Artwork loading
    implementation(libs.glide)

    // Coroutines
    implementation(libs.coroutines.android)

    // Media3 podcast playback
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)

    // Networking extras
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
}