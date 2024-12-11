plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("de.undercouch.download")
}

val ASSET_DIR = file("$projectDir/src/main/assets")

// Set the properties in extra
extra["ASSET_DIR"] = ASSET_DIR

apply(from = "./download_models.gradle")

android {
    namespace = "com.ashu.photodescriber"
    compileSdk = 35

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("src/main/jniLibs")
        }
    }

    ndkVersion = "21.4.7075529"

    defaultConfig {

        applicationId = "com.ashu.photodescriber"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }

        externalNativeBuild {
            cmake {
                cppFlags("-std=c++11")
            }
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    externalNativeBuild {
        cmake {
            path = file("CMakeLists.txt")
        }
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
        viewBinding = true
    }
    androidResources {
        noCompress("tflite")
    }

}


dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.hilt.android)

    implementation("com.google.mediapipe:tasks-vision:0.10.18")


    kapt(libs.hilt.android.compiler)

    // Permission
    implementation (libs.accompanist.permissions)

    // Room
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)

    kapt(libs.androidx.room.compiler.v221)
    implementation("com.google.code.gson:gson:2.11.0")

    implementation("com.squareup.picasso:picasso:2.71828")

    // Guild
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    testImplementation("org.mockito:mockito-inline:3.5.13")  // includes "core"
    testImplementation("org.mockito:mockito-junit-jupiter:3.5.13")
}