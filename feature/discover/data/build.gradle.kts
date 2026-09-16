plugins {
    id("artgallery.android.library")
}

android {
    namespace = "com.novack.artgalleryv2.feature.discover.data"
}

dependencies {
    implementation(project(":core:datastore"))
    implementation(project(":feature:discover:domain"))
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)

    testImplementation(libs.junit)
    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
}
