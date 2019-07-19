package com.example.android.trackmysleepquality

import android.app.Application
import timber.log.Timber

/**
 *****************************************************************
 * Created By muban on 7/19/2019
 *****************************************************************
 */
class TrackMySleepApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Enamble Timber Logging
        Timber.plant(Timber.DebugTree())
    }
}