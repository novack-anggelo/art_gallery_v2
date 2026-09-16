package com.novack.artgalleryv2.di

import com.novack.artgalleryv2.BuildConfig
import com.novack.artgalleryv2.core.data.di.dataModule
import com.novack.artgalleryv2.core.data.remote.di.artworkApiModule
import com.novack.artgalleryv2.core.network.di.networkModule
import com.novack.artgalleryv2.ui.feature.discover.di.discoverModule
import org.koin.core.module.Module

val appModules: List<Module> = listOf(
    networkModule(isDebug = BuildConfig.DEBUG),
    artworkApiModule,
    dataModule,
    discoverModule,
)
