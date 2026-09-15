package com.novack.artgalleryv2.core.domain.repository

import com.novack.artgalleryv2.core.domain.model.DiscoverPreferenceAction
import com.novack.artgalleryv2.core.domain.model.DiscoverPreferenceTransactionResult
import com.novack.artgalleryv2.core.domain.model.DiscoverPreferences
import kotlinx.coroutines.flow.Flow

interface DiscoverPreferencesRepository {
    val preferences: Flow<DiscoverPreferences>

    suspend fun applyActions(
        actions: List<DiscoverPreferenceAction>,
    ): DiscoverPreferenceTransactionResult

    suspend fun replace(preferences: DiscoverPreferences)
}
