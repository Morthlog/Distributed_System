plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.customerapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.customerapp"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.material.v1110)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(files("..\\libs\\lib.jar"))
    testImplementation(libs.junit)
    implementation(files("..\\libs\\json-simple-1.1.1.jar"))
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}