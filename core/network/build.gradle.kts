plugins {
    id("artgallery.kotlin.library")
}

dependencies {
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp.logging)

    testImplementation(libs.junit)
    testImplementation(libs.koin.test.junit4)
}
