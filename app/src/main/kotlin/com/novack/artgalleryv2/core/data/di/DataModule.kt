package com.novack.artgalleryv2.core.data.di

import com.novack.artgalleryv2.core.data.repository.NetworkArtworkRepository
import com.novack.artgalleryv2.core.domain.repository.ArtworkRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataModule = module {
    singleOf(::NetworkArtworkRepository) {
        bind<ArtworkRepository>()
    }
}
