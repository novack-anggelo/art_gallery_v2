package com.novack.artgalleryv2

import android.app.Application
import com.novack.artgalleryv2.di.appModules
import com.novack.artgalleryv2.firebase.FirebaseAppCheckInitializer
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ArtGalleryApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        FirebaseAppCheckInitializer.initialize(this)

        startKoin {
            androidContext(this@ArtGalleryApplication)
            modules(appModules)
        }
    }
}
