package com.novack.artgalleryv2.feature.discover.domain.interpreter

import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPreferenceAction
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPreferences

fun interface DiscoverCustomizationInterpreter {
    suspend fun interpret(request: DiscoverCustomizationRequest): DiscoverCustomizationInterpretation
}

data class DiscoverCustomizationRequest(
    val prompt: String,
    val currentPreferences: DiscoverPreferences,
)

sealed interface DiscoverCustomizationInterpretation {
    data class Supported(val actions: List<DiscoverPreferenceAction>) :
        DiscoverCustomizationInterpretation

    data class Rejected(val reason: DiscoverCustomizationRejection) :
        DiscoverCustomizationInterpretation
}

enum class DiscoverCustomizationRejection {
    InvalidRequest,
    UnsupportedRequest,
    InvalidResponse,
    NetworkUnavailable,
    QuotaExceeded,
    ServiceUnavailable,
}
