package com.novack.artgalleryv2.feature.discover.data.interpreter

import com.novack.artgalleryv2.feature.discover.domain.interpreter.DiscoverCustomizationInterpretation
import com.novack.artgalleryv2.feature.discover.domain.interpreter.DiscoverCustomizationRejection
import com.novack.artgalleryv2.feature.discover.domain.interpreter.DiscoverCustomizationRequest
import com.novack.artgalleryv2.feature.discover.domain.model.ArtworkMetadataField
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPreferenceAction
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPreferences
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPresentation
import com.novack.artgalleryv2.feature.discover.domain.model.ResizeDirection
import com.novack.artgalleryv2.feature.discover.domain.usecase.InterpretDiscoverCustomizationUseCase
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FirebaseDiscoverCustomizationInterpreterTest {
    @Test
    fun `valid response maps supported actions and sends current preferences`() = runTest {
        var sentPrompt = ""
        val interpreter = interpreter {
            sentPrompt = it
            """{"supported":true,"actions":[{"type":"resize_presentation","direction":"smaller"},{"type":"set_metadata_visibility","field":"artist","visible":false}]}"""
        }
        val request = DiscoverCustomizationRequest(
            prompt = "Make images smaller",
            currentPreferences = DiscoverPreferences(DiscoverPresentation.CompactGrid),
        )
        val result = assertIs<DiscoverCustomizationInterpretation.Supported>(
            interpreter.interpret(request),
        )
        assertEquals(
            listOf(
                DiscoverPreferenceAction.ResizePresentation(ResizeDirection.Smaller),
                DiscoverPreferenceAction.SetMetadataVisibility(ArtworkMetadataField.Artist, false),
            ),
            result.actions,
        )
        assertTrue(sentPrompt.contains("Current presentation: CompactGrid"))
        assertTrue(sentPrompt.contains("User request: \"Make images smaller\""))
    }

    @Test
    fun `unsupported and invalid responses are rejected`() = runTest {
        val cases = mapOf(
            """{"supported":false,"actions":[]}""" to
                DiscoverCustomizationRejection.UnsupportedRequest,
            """{"supported":true,"actions":[]}""" to
                DiscoverCustomizationRejection.InvalidResponse,
            """{"supported":true,"actions":[{"type":"unknown"}]}""" to
                DiscoverCustomizationRejection.InvalidResponse,
            "not json" to DiscoverCustomizationRejection.InvalidResponse,
        )
        cases.forEach { (response, expected) ->
            val result = assertIs<DiscoverCustomizationInterpretation.Rejected>(
                interpreter { response }.interpret(defaultRequest),
            )
            assertEquals(expected, result.reason)
        }
    }

    @Test
    fun `provider failures are mapped without exposing implementation errors`() = runTest {
        val network = assertIs<DiscoverCustomizationInterpretation.Rejected>(
            interpreter { throw IOException() }.interpret(defaultRequest),
        )
        val unexpected = assertIs<DiscoverCustomizationInterpretation.Rejected>(
            interpreter { error("failure") }.interpret(defaultRequest),
        )
        assertEquals(DiscoverCustomizationRejection.NetworkUnavailable, network.reason)
        assertEquals(DiscoverCustomizationRejection.ServiceUnavailable, unexpected.reason)
    }

    @Test
    fun `invalid prompt is rejected before calling the interpreter`() = runTest {
        var called = false
        val useCase = InterpretDiscoverCustomizationUseCase { called = true; error("called") }
        assertEquals(invalidRequest, useCase(" ", DiscoverPreferences()))
        assertEquals(false, called)
    }

    private fun interpreter(response: suspend (String) -> String?) =
        FirebaseDiscoverCustomizationInterpreter(DiscoverCustomizationModel(response))
    private val defaultRequest = DiscoverCustomizationRequest("smaller", DiscoverPreferences())
    private val invalidRequest = DiscoverCustomizationInterpretation.Rejected(
        DiscoverCustomizationRejection.InvalidRequest,
    )
}
