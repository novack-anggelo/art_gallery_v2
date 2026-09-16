plugins {
    id("artgallery.android.library")
}

android {
    namespace = "com.novack.artgalleryv2.core.datastore"
}

dependencies {
    api(libs.androidx.datastore.preferences)
}
