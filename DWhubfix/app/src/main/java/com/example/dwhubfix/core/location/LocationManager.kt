package com.example.dwhubfix.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Location Manager for getting device location
 *
 * This class provides a coroutine-friendly API for getting the current device location
 * using Google Play Services FusedLocationProviderClient. It handles permission checks
 * and provides proper error handling.
 *
 * Usage:
 * ```kotlin
 * val result = locationManager.getCurrentLocation()
 * result.onSuccess { location ->
 *     val lat = location.latitude
 *     val lng = location.longitude
 * }
 * ```
 */
@Singleton
class LocationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Get the current device location
     *
     * This method uses getCurrentLocation() which returns a cached location if available,
     * or fetches a new one. This is more efficient than requesting fresh location each time.
     *
     * @return Result containing Location on success, or Exception on failure
     */
    suspend fun getCurrentLocation(): Result<Location> = suspendCancellableCoroutine { cont ->
        try {
            // Check permissions first
            if (!hasLocationPermission()) {
                cont.resume(Result.failure(SecurityException("Location permission not granted")))
                return@suspendCancellableCoroutine
            }

            val cancellationTokenSource = CancellationTokenSource()

            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    cont.resume(Result.success(location))
                } else {
                    cont.resume(Result.failure(LocationUnavailableException("Unable to get location. Try again.")))
                }
            }.addOnFailureListener { e ->
                cont.resume(Result.failure(e))
            }

            cont.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
        } catch (e: Exception) {
            cont.resumeWithException(e)
        }
    }

    /**
     * Check if the app has location permissions
     *
     * @return true if either FINE or COARSE location permission is granted
     */
    fun hasLocationPermission(): Boolean {
        return hasFineLocationPermission() || hasCoarseLocationPermission()
    }

    /**
     * Check if the app has fine location permission
     */
    fun hasFineLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if the app has coarse location permission
     */
    fun hasCoarseLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Get a fallback/default location
     *
     * This should only be used as a fallback when location is unavailable.
     * Default location is set to Bali center: -8.5069, 115.2625
     *
     * @return Location object with default coordinates
     */
    fun getDefaultLocation(): Location {
        return Location("default").apply {
            latitude = -8.5069
            longitude = 115.2625
            accuracy = 1000.0f // 1km accuracy (low confidence)
        }
    }
}

/**
 * Exception thrown when location is unavailable
 */
class LocationUnavailableException(message: String) : Exception(message)
