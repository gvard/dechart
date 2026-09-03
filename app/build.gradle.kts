plugins {
    id("com.android.application")
}

android {
    namespace = "ru.ex.dechart"
    compileSdk = 34

    defaultConfig {
        applicationId = "ru.ex.dechart"
        minSdk = 21  // Android 5.0 - modern minimum
        targetSdk = 34
        versionCode = 2
        versionName = "0.2.0-modernized"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = false
        aidl = false
    }
    sourceSets {
        getByName("debug") {
            assets {
                srcDirs("src\\debug\\assets")
            }
        }
    }
}

dependencies {

    // Core Android libraries
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")


    // AChartEngine library, latest version
    implementation(files("libs/achartengine-1.2.0.jar"))

    // For file access (modern alternatives)
    implementation("androidx.documentfile:documentfile:1.0.1")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
