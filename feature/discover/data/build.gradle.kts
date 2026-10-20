plugins {
    id("artgallery.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.novack.artgalleryv2.feature.discover.data"
}

dependencies {
    implementation(project(":core:datastore"))
    implementation(project(":feature:discover:domain"))
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.ai)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
}
