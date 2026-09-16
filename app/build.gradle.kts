plugins {
    id("artgallery.android.application")
    id("artgallery.android.compose")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.novack.artgalleryv2"

    defaultConfig {
        applicationId = "com.novack.artgalleryv2"
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:artwork:data"))
    implementation(project(":core:artwork:domain"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:network"))
    implementation(project(":feature:discover:data"))
    implementation(project(":feature:discover:domain"))
    implementation(project(":feature:discover:presentation"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    testImplementation(libs.junit)
    testImplementation(kotlin("test"))

    // Dependency injection
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)

// Serialization
    implementation(libs.kotlinx.serialization.json)

// Lifecycle and navigation
    implementation(libs.androidx.navigation.compose)

// Local tests
    testImplementation(libs.mockk)
}
