package com.novack.artgalleryv2.feature.discover.presentation.di

import com.novack.artgalleryv2.feature.discover.domain.usecase.CoordinateDiscoverPreferenceChangesUseCase
import com.novack.artgalleryv2.feature.discover.presentation.DiscoverViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val discoverModule = module {
    factoryOf(::CoordinateDiscoverPreferenceChangesUseCase)
    viewModelOf(::DiscoverViewModel)
}
