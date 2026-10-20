package com.novack.artgalleryv2.feature.discover.domain.usecase

import com.novack.artgalleryv2.feature.discover.domain.interpreter.DiscoverCustomizationInterpretation
import com.novack.artgalleryv2.feature.discover.domain.interpreter.DiscoverCustomizationInterpreter
import com.novack.artgalleryv2.feature.discover.domain.interpreter.DiscoverCustomizationRejection
import com.novack.artgalleryv2.feature.discover.domain.interpreter.DiscoverCustomizationRequest
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPreferences

class InterpretDiscoverCustomizationUseCase(
    private val interpreter: DiscoverCustomizationInterpreter,
) {
    suspend operator fun invoke(
        prompt: String,
        currentPreferences: DiscoverPreferences,
    ): DiscoverCustomizationInterpretation {
        val normalizedPrompt = prompt.trim()
        if (normalizedPrompt.isEmpty() || normalizedPrompt.length > MAX_PROMPT_LENGTH) {
            return DiscoverCustomizationInterpretation.Rejected(
                reason = DiscoverCustomizationRejection.InvalidRequest,
            )
        }
        return interpreter.interpret(
            DiscoverCustomizationRequest(normalizedPrompt, currentPreferences),
        )
    }

    private companion object {
        const val MAX_PROMPT_LENGTH = 500
    }
}
