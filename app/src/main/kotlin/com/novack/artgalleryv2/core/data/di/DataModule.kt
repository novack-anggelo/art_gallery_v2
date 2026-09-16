package com.novack.artgalleryv2.core.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.novack.artgalleryv2.core.data.local.DataStoreDiscoverPreferencesRepository
import com.novack.artgalleryv2.core.domain.repository.DiscoverPreferencesRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataModule = module {
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.create {
            androidContext().preferencesDataStoreFile("discover_preferences")
        }
    }
    singleOf(::DataStoreDiscoverPreferencesRepository) {
        bind<DiscoverPreferencesRepository>()
    }
}
