package com.novack.artgalleryv2.di

import com.novack.artgalleryv2.core.network.di.networkModule
import org.koin.core.module.Module

val appModules: List<Module> = listOf(
    networkModule,
)