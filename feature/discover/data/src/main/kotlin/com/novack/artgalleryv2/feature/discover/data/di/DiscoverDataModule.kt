package com.novack.artgalleryv2.feature.discover.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import com.novack.artgalleryv2.core.datastore.createPreferencesDataStore
import com.novack.artgalleryv2.feature.discover.data.interpreter.DiscoverCustomizationModel
import com.novack.artgalleryv2.feature.discover.data.interpreter.FirebaseDiscoverCustomizationInterpreter
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
    single<GenerativeModel> {
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = "gemini-3.5-flash-lite",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = discoverCustomizationSchema
            },
        )
    }
    single<DiscoverCustomizationModel> {
        val model = get<GenerativeModel>()
        DiscoverCustomizationModel { model.generateContent(it).text }
    }
    singleOf(::FirebaseDiscoverCustomizationInterpreter) {
        bind<DiscoverCustomizationInterpreter>()
    }
    factory { InterpretDiscoverCustomizationUseCase(get()) }
}

private val actionTypes = listOf(
    "set_presentation", "resize_presentation", "set_metadata_visibility",
)

private val discoverCustomizationSchema = Schema.obj(
    mapOf(
        "supported" to Schema.boolean(),
        "actions" to Schema.array(
            Schema.obj(
                properties = mapOf(
                    "type" to Schema.enumeration(actionTypes),
                    "presentation" to Schema.enumeration(
                        listOf("large_grid", "compact_grid", "thumbnail_rows"),
                    ),
                    "direction" to Schema.enumeration(listOf("smaller", "larger")),
                    "field" to Schema.enumeration(listOf("artist", "date", "medium")),
                    "visible" to Schema.boolean(),
                ),
                optionalProperties = listOf("presentation", "direction", "field", "visible"),
            ),
        ),
    ),
)
