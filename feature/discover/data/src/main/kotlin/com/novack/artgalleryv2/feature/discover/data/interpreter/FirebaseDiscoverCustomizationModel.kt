package com.novack.artgalleryv2.feature.discover.data.interpreter

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig

internal fun createFirebaseDiscoverCustomizationModel(): DiscoverCustomizationModel {
    val model = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
        modelName = FIREBASE_MODEL_NAME,
        generationConfig = generationConfig {
            responseMimeType = JSON_RESPONSE_MIME_TYPE
            responseSchema = DiscoverCustomizationContract.responseSchema
        },
    )
    return DiscoverCustomizationModel { model.generateContent(it).text }
}

internal object DiscoverCustomizationContract {
    const val SUPPORTED = "supported"
    const val ACTIONS = "actions"
    const val TYPE = "type"
    const val PRESENTATION = "presentation"
    const val DIRECTION = "direction"
    const val FIELD = "field"
    const val VISIBLE = "visible"
    const val SET_PRESENTATION = "set_presentation"
    const val RESIZE_PRESENTATION = "resize_presentation"
    const val SET_METADATA_VISIBILITY = "set_metadata_visibility"
    const val LARGE_GRID = "large_grid"
    const val COMPACT_GRID = "compact_grid"
    const val THUMBNAIL_ROWS = "thumbnail_rows"
    const val SMALLER = "smaller"
    const val LARGER = "larger"
    const val ARTIST = "artist"
    const val DATE = "date"
    const val MEDIUM = "medium"

    val responseSchema = Schema.obj(
        mapOf(
            SUPPORTED to Schema.boolean(),
            ACTIONS to Schema.array(
                Schema.obj(
                    properties = mapOf(
                        TYPE to Schema.enumeration(
                            listOf(SET_PRESENTATION, RESIZE_PRESENTATION, SET_METADATA_VISIBILITY),
                        ),
                        PRESENTATION to Schema.enumeration(
                            listOf(LARGE_GRID, COMPACT_GRID, THUMBNAIL_ROWS),
                        ),
                        DIRECTION to Schema.enumeration(listOf(SMALLER, LARGER)),
                        FIELD to Schema.enumeration(listOf(ARTIST, DATE, MEDIUM)),
                        VISIBLE to Schema.boolean(),
                    ),
                    optionalProperties = listOf(PRESENTATION, DIRECTION, FIELD, VISIBLE),
                ),
            ),
        ),
    )
}

private const val FIREBASE_MODEL_NAME = "gemini-3.5-flash-lite"
private const val JSON_RESPONSE_MIME_TYPE = "application/json"
