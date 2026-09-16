package com.novack.artgalleryv2.feature.discover.domain.repository

import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPreferenceAction
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPreferenceTransactionResult
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPreferences
import kotlinx.coroutines.flow.Flow

interface DiscoverPreferencesRepository {
    val preferences: Flow<DiscoverPreferences>

    suspend fun applyActions(
        actions: List<DiscoverPreferenceAction>,
    ): DiscoverPreferenceTransactionResult

    suspend fun replace(preferences: DiscoverPreferences)
}
