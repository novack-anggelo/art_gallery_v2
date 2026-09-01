package com.novack.artgalleryv2.di

import com.novack.artgalleryv2.core.data.di.networkModule
import org.koin.core.module.Module

val appModules: List<Module> = listOf(
    networkModule,
)