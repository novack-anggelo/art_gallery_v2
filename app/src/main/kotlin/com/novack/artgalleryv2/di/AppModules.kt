package com.novack.artgalleryv2.di

import com.novack.artgalleryv2.core.data.remote.di.networkModule
import org.koin.core.module.Module

val appModules: List<Module> = listOf(
    networkModule,
)