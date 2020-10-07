package tv.superawesome.sdk.publisher.ui.di

import org.koin.core.module.Module
import org.koin.dsl.module
import tv.superawesome.sdk.publisher.ui.common.AdController
import tv.superawesome.sdk.publisher.ui.common.AdControllerType
import tv.superawesome.sdk.publisher.ui.common.ParentalGate

fun createUiModule(): Module = module {
    factory<AdControllerType> { AdController(get(), get(), get()) }
    factory { ParentalGate(get(), get()) }
}