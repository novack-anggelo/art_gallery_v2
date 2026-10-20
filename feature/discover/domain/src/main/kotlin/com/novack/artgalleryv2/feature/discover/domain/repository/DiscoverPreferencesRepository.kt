package com.novack.artgalleryv2.feature.discover.domain.repository

import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPreferenceAction
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPreferenceTransactionResult
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

enum class DiscoverPreferencesStorageStatus {
    Loading,
    Available,
    Unavailable,
}

sealed interface DiscoverPreferencesStorageResult<out T> {
    data class Success<T>(val value: T) : DiscoverPreferencesStorageResult<T>
    data object Unavailable : DiscoverPreferencesStorageResult<Nothing>
}

interface DiscoverPreferencesRepository {
    val preferences: Flow<DiscoverPreferences>
    val storageStatus: StateFlow<DiscoverPreferencesStorageStatus>

    suspend fun applyActions(
        actions: List<DiscoverPreferenceAction>,
    ): DiscoverPreferencesStorageResult<DiscoverPreferenceTransactionResult>

    suspend fun replace(
        preferences: DiscoverPreferences,
    ): DiscoverPreferencesStorageResult<Unit>
}
