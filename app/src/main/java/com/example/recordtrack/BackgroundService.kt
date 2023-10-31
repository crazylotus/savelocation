package com.example.recordtrack

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi


class BackgroundService : Service() {

    // if app in background still used to fetch the current location in this project  not used this class

    private val binder = LocationServiceBinder()
    private val TAG = "BackgroundService"
    private var mLocationListener: LocationListener? = null
    private var mLocationManager: LocationManager? = null
    private val notificationManager: NotificationManager? = null

    private val LOCATION_INTERVAL = 500
    private val LOCATION_DISTANCE = 10

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    private class LocationListener(provider: String?) : android.location.LocationListener {
        private val lastLocation: Location? = null
        private val TAG = "LocationListener"
        private var mLastLocation: Location

        init {
            mLastLocation = Location(provider)
        }

        override fun onLocationChanged(location: Location) {
            mLastLocation = location

            Log.i(TAG, "LocationChanged: $location")
        }

        override fun onProviderDisabled(provider: String) {
            Log.e(TAG, "onProviderDisabled: $provider")
        }

        override fun onProviderEnabled(provider: String) {
            Log.e(TAG, "onProviderEnabled: $provider")
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            Log.e(TAG, "onStatusChanged: $status")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        Log.i(TAG, "onCreate")
        startForeground(12345678, getNotification())
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mLocationManager != null) {
            try {
                mLocationManager!!.removeUpdates(mLocationListener!!)
            } catch (ex: Exception) {
                Log.i(TAG, "fail to remove location listners, ignore", ex)
            }
        }
    }

    private fun initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager =
                applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager
        }
    }

    fun startTracking() {
        initializeLocationManager()
        mLocationListener = LocationListener(LocationManager.GPS_PROVIDER)
        try {
            mLocationManager!!.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                LOCATION_INTERVAL.toLong(),
                LOCATION_DISTANCE.toFloat(),
                mLocationListener!!
            )
        } catch (ex: SecurityException) {
            // Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (ex: IllegalArgumentException) {
            // Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    fun stopTracking() {
        onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getNotification(): Notification? {
        val channel =
            NotificationChannel("channel_01", "My Channel", NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManager: NotificationManager = getSystemService(
            NotificationManager::class.java
        )
        notificationManager.createNotificationChannel(channel)
        val builder =
            Notification.Builder(getApplicationContext(), "channel_01").setAutoCancel(true)
        return builder.build()
    }

    inner class LocationServiceBinder : Binder() {
        val service: BackgroundService
            get() = this@BackgroundService
    }
}