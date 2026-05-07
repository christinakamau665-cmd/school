package com.emobilis.app.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.emobilis.app.AppConstants
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object LocationHelper {

    suspend fun isStudentAtSchool(context: Context): Boolean {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return false

        val location = getCurrentLocation(context) ?: return false

        val schoolLocation = Location("school").apply {
            latitude = AppConstants.SCHOOL_LATITUDE
            longitude = AppConstants.SCHOOL_LONGITUDE
        }
        return location.distanceTo(schoolLocation) <= AppConstants.SCHOOL_RADIUS_METERS
    }

    @Suppress("MissingPermission")
    private suspend fun getCurrentLocation(context: Context): Location? =
        suspendCancellableCoroutine { cont ->
            LocationServices.getFusedLocationProviderClient(context)
                .lastLocation
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resume(null) }
        }
}
