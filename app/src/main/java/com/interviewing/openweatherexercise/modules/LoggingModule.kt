package com.interviewing.openweatherexercise.modules

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import timber.log.Timber.DebugTree

/**
 * Timber will use normal debug logs
 *
 * TODO: In a real version, this would live in release / debug targets, rather than just here.
 */
@Module
@InstallIn(SingletonComponent::class)
class LoggingModule {

    @Provides
    fun loggingTrees(): List<@JvmSuppressWildcards Timber.Tree> = listOf(DebugTree())
}