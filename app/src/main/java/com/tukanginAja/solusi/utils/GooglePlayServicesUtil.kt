package com.tukanginAja.solusi.utils

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

object GooglePlayServicesUtil {
    /**
     * Check if Google Play Services is available and up to date
     * @return true if available, false otherwise
     */
    fun isGooglePlayServicesAvailable(context: Context): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
        return resultCode == ConnectionResult.SUCCESS
    }
    
    /**
     * Get error message if Google Play Services is not available
     */
    fun getErrorMessage(context: Context): String? {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
        
        return if (resultCode != ConnectionResult.SUCCESS) {
            googleApiAvailability.getErrorString(resultCode)
        } else {
            null
        }
    }
    
    /**
     * Check if Google Play Services needs to be updated
     */
    fun isUserResolvableError(context: Context): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
        return googleApiAvailability.isUserResolvableError(resultCode)
    }
}

