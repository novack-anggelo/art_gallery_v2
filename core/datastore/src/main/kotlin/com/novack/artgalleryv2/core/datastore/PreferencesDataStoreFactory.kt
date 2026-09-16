package com.novack.artgalleryv2.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile

fun createPreferencesDataStore(
    context: Context,
    name: String,
): DataStore<Preferences> = PreferenceDataStoreFactory.create {
    context.preferencesDataStoreFile(name)
}
