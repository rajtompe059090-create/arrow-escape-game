plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.arrowescape.game"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.arrowescape.game"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            val keystorePath = project.findProperty("KEYSTORE_FILE") as? String
                ?: System.getenv("KEYSTORE_FILE")
            val keystorePassword = project.findProperty("KEYSTORE_PASSWORD") as? String
                ?: System.getenv("KEYSTORE_PASSWORD")
            val keyAliasStr = project.findProperty("KEY_ALIAS") as? String
                ?: System.getenv("KEY_ALIAS")
            val keyPasswordStr = project.findProperty("KEY_PASSWORD") as? String
                ?: System.getenv("KEY_PASSWORD")

            if (!keystorePath.isNullOrBlank() && file(keystorePath).exists()) {
                storeFile = file(keystorePath)
                storePassword = keystorePassword
                keyAlias = keyAliasStr
                keyPassword = keyPasswordStr
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Only attach release signing if production keystore is supplied
            val releaseConfig = signingConfigs.getByName("release")
            if (releaseConfig.storeFile != null) {
                signingConfig = releaseConfig
            }
        }

        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)

        compilerOptions {
            jvmTarget.set(
                org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")

    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Google Mobile Ads SDK
    implementation("com.google.android.gms:play-services-ads:23.6.0")
}
