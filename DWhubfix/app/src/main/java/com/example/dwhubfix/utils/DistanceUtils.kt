package com.example.dwhubfix.utils

import kotlin.math.*

/**
 * Calculate distance between two coordinates using Haversine formula
 * @param lat1 First coordinate latitude
 * @param lon1 First coordinate longitude
 * @param lat2 Second coordinate latitude
 * @param lon2 Second coordinate longitude
 * @return Distance in kilometers
 */
fun calculateDistance(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
): Double {
    val earthRadius = 6371 // km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    
    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2)
    
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    
    return earthRadius * c
}

/**
 * Format distance for display
 * @param distanceInKm Distance in kilometers
 * @return Formatted string (e.g., "0.8 km")
 */
fun formatDistance(distanceInKm: Double): String {
    return when {
        distanceInKm < 1.0 -> String.format("%.1f km", distanceInKm)
        else -> String.format("%.1f km", distanceInKm)
    }
}

/**
 * Calculate distance between two GeoPoints
 * @param point1 First GeoPoint
 * @param point2 Second GeoPoint
 * @return Distance in kilometers
 */
fun calculateDistance(
    point1: org.osmdroid.util.GeoPoint,
    point2: org.osmdroid.util.GeoPoint
): Double {
    return calculateDistance(
        point1.latitude,
        point1.longitude,
        point2.latitude,
        point2.longitude
    )
}
