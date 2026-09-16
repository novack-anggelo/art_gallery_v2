package com.novack.artgalleryv2.ui.feature.discover.di

import com.novack.artgalleryv2.feature.discover.domain.usecase.CoordinateDiscoverPreferenceChangesUseCase
import com.novack.artgalleryv2.ui.feature.discover.DiscoverViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val discoverModule = module {
    factoryOf(::CoordinateDiscoverPreferenceChangesUseCase)
    viewModelOf(::DiscoverViewModel)
}
