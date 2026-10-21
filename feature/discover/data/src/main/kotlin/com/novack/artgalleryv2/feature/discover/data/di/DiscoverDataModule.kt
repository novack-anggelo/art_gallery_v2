package com.novack.artgalleryv2.feature.discover.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.novack.artgalleryv2.core.datastore.createPreferencesDataStore
import com.novack.artgalleryv2.feature.discover.data.interpreter.DiscoverCustomizationModel
import com.novack.artgalleryv2.feature.discover.data.interpreter.FirebaseDiscoverCustomizationInterpreter
import com.novack.artgalleryv2.feature.discover.data.interpreter.createFirebaseDiscoverCustomizationModel
import com.novack.artgalleryv2.feature.discover.data.repository.DataStoreDiscoverPreferencesRepository
import com.novack.artgalleryv2.feature.discover.domain.interpreter.DiscoverCustomizationInterpreter
import com.novack.artgalleryv2.feature.discover.domain.repository.DiscoverPreferencesRepository
import com.novack.artgalleryv2.feature.discover.domain.usecase.InterpretDiscoverCustomizationUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val discoverDataModule = module {
    single<DataStore<Preferences>> {
        createPreferencesDataStore(
            context = androidContext(),
            name = "discover_preferences",
        )
    }
    singleOf(::DataStoreDiscoverPreferencesRepository) {
        bind<DiscoverPreferencesRepository>()
    }
    single<DiscoverCustomizationModel> { createFirebaseDiscoverCustomizationModel() }
    singleOf(::FirebaseDiscoverCustomizationInterpreter) {
        bind<DiscoverCustomizationInterpreter>()
    }
    factory { InterpretDiscoverCustomizationUseCase(get()) }
}
