plugins {
    `kotlin-dsl`
}

group = "com.novack.artgallery.buildlogic"

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "artgallery.android.application"
            implementationClass = "com.novack.artgallery.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "artgallery.android.library"
            implementationClass = "com.novack.artgallery.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "artgallery.android.compose"
            implementationClass = "com.novack.artgallery.buildlogic.AndroidComposeConventionPlugin"
        }
        register("kotlinLibrary") {
            id = "artgallery.kotlin.library"
            implementationClass = "com.novack.artgallery.buildlogic.KotlinLibraryConventionPlugin"
        }
    }
}
