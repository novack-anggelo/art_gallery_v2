package com.novack.artgalleryv2.core.domain.repository

import com.novack.artgalleryv2.core.domain.model.DiscoverPreferenceAction
import com.novack.artgalleryv2.core.domain.model.DiscoverPreferenceResult
import com.novack.artgalleryv2.core.domain.model.DiscoverPreferences
import kotlinx.coroutines.flow.Flow

interface DiscoverPreferencesRepository {
    val preferences: Flow<DiscoverPreferences>

    suspend fun applyAction(action: DiscoverPreferenceAction): DiscoverPreferenceResult

    suspend fun replace(preferences: DiscoverPreferences)
}
