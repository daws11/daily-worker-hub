package com.example.dwhubfix

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Custom Application class for DailyWorkerHub
 *
 * This class is annotated with @HiltAndroidApp which triggers Hilt's code generation,
 * including a base class for the application that serves as the application-level
 * dependency container.
 *
 * Make sure to declare this in AndroidManifest.xml:
 * ```xml
 * <application
 *     android:name=".DWHubApplication"
 *     ...>
 * </application>
 * ```
 */
@HiltAndroidApp
class DWHubApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize any app-wide components here
    }
}
