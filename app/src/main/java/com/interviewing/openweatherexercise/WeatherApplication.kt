package com.interviewing.openweatherexercise

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber.Forest
import timber.log.Timber.Tree
import javax.inject.Inject

@HiltAndroidApp
class WeatherApplication : Application() {

    @Inject lateinit var loggingForest: List<@JvmSuppressWildcards Tree>

    override fun onCreate() {
        super.onCreate()
        Forest.plant(*loggingForest.toTypedArray())
    }
}