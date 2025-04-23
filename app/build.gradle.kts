plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id ("kotlin-kapt")
    id ("dagger.hilt.android.plugin")
    id ("com.google.gms.google-services")
}

android {
    namespace = "com.kosiso.smartcount"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.kosiso.smartcount"
        minSdk = 26
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

//    implementation (libs.androidx.camera.core)
//    implementation (libs.androidx.camera.camera2)
//    implementation (libs.androidx.camera.lifecycle)
//    implementation (libs.androidx.camera.view)

    // firebase
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    //geofirestore
    implementation ("com.github.imperiumlabs:GeoFirestore-Android:v1.5.0")

    // Google Maps Location Services
    implementation ("com.google.android.gms:play-services-location:21.1.0")


    implementation("androidx.navigation:navigation-compose:2.7.7")
//
    implementation ("com.google.accompanist:accompanist-permissions:0.34.0")

    val cameraxVersion = "1.3.0-rc01"

    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-video:$cameraxVersion")

    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("androidx.camera:camera-extensions:$cameraxVersion")

    // Guava dependency for ListenableFuture
    implementation ("com.google.guava:guava:31.0.1-android")

    // Required if you're using Java 8+
    implementation ("androidx.concurrent:concurrent-futures:1.1.0")


    //charts
    implementation ("co.yml:ycharts:2.1.0")

//    Google ML Kit Face detection
    implementation ("com.google.android.gms:play-services-mlkit-face-detection:17.1.0")
//    implementation ("com.google.mlkit:face-detection:16.1.7")

    implementation ("androidx.constraintlayout:constraintlayout-compose:1.1.0")


    // ViewModel support in Compose
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Kotlin Coroutines for ViewModel and StateFlow
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // Hilt dependencies
    implementation ("com.google.dagger:hilt-android:2.48.1")
    kapt ("com.google.dagger:hilt-compiler:2.48.1")

    // ViewModel with Hilt integration
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.0")

    // lifecycle service
    implementation("androidx.lifecycle:lifecycle-service:2.8.7")

    implementation ("androidx.compose.runtime:runtime-livedata:1.7.6")

    //media session
    implementation ("androidx.media:media:1.5.0")

    // Room
    implementation ("androidx.room:room-runtime:2.6.1")
    kapt ("androidx.room:room-compiler:2.6.1")

    implementation ("com.google.code.gson:gson:2.10.1")

    // Coroutines support for Room
    implementation ("androidx.room:room-ktx:2.6.1")

}