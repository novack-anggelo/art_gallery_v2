package com.novack.artgalleryv2.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.novack.artgalleryv2.navigation.ArtGalleryNavHost
import com.novack.artgalleryv2.ui.theme.Art_gallery_v2Theme

@Composable
internal fun ArtGalleryApp() {
    Art_gallery_v2Theme {
        val navController = rememberNavController()

        ArtGalleryNavHost(
            navController = navController
        )
    }
}
