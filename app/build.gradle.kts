plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    kotlin("plugin.serialization") version "2.0.21"
    id ("kotlin-parcelize")
}

android {
    namespace = "com.diplomaproject.litefood"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.diplomaproject.litefood"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

}


dependencies {
    implementation(platform(libs.firebase.bom.v3230))
    implementation(libs.bundles.firebase)

    implementation(libs.lifecycle.runtime.ktx)

    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

    implementation(libs.material3)
    implementation(libs.material)

    implementation(libs.androidx.animation.core.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.ui.tooling.preview)

    implementation(libs.espresso.core)
    androidTestImplementation(libs.espresso.core)

    implementation(libs.core)
    implementation(libs.play.services.maps)

    androidTestImplementation(libs.junit.jupiter)
    implementation(libs.appcompat)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)


    implementation(libs.decoro)
    testImplementation(libs.junit.jupiter.v570)
    testImplementation(libs.mockito.core)

    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.4.1")
    implementation("androidx.lifecycle:lifecycle-livedata:2.4.1")
    implementation("androidx.databinding:databinding-runtime:7.0.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")

    implementation ("com.stripe:stripe-android:+")
    implementation ("androidx.appcompat:appcompat:1.6.0")

    implementation (libs.glide)


    val nav_version = "2.8.5"
    implementation("androidx.navigation:navigation-fragment:$nav_version")
    implementation("androidx.navigation:navigation-ui:$nav_version")
    implementation("androidx.navigation:navigation-dynamic-features-fragment:$nav_version")
    androidTestImplementation("androidx.navigation:navigation-testing:$nav_version")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation ("org.jetbrains.kotlin:kotlin-stdlib:2.1.10")

    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    implementation("com.google.android.gms:play-services-maps:19.1.0")
    implementation ("com.google.android.gms:play-services-location:21.0.1")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation(libs.shimmer)
}