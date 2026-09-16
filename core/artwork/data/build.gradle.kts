plugins {
    id("artgallery.kotlin.library")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(project(":core:artwork:domain"))
    implementation(project(":core:network"))
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(platform(libs.retrofit.bom))
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp.logging)
    implementation(libs.androidx.paging.common)

    testImplementation(libs.junit)
    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.paging.testing)
}
