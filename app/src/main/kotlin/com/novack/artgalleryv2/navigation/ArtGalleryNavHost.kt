package com.novack.artgalleryv2.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.novack.artgalleryv2.feature.discover.presentation.DiscoverRoute
import kotlinx.serialization.Serializable

@Serializable
internal data object DiscoverDestination

@Composable
internal fun ArtGalleryNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = DiscoverDestination,
        modifier = modifier
    ) {
        composable<DiscoverDestination> {
            DiscoverRoute(
                onArtworkClick = {} // TODO: Implement
            )
        }
    }
}
