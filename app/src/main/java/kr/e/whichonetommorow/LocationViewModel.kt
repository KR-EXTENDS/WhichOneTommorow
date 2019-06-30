package kr.e.whichonetommorow

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.*

class LocationViewModel(application: Application) : AndroidViewModel(Application()), LifecycleObserver {

    private var mLocationManager: LocationManager? = null
    private val mLocationListener = MyLocationListener()
    private val mContext = application
    var mLocation = MutableLiveData<Location>()

    @SuppressLint("MissingPermission")
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun addLocationListener() {
        mLocationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mLocationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0F, mLocationListener)
        val lastLocation = mLocationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        // Javaから呼び出してるためnullチェック
        if (this.mLocationListener != null) this.mLocationListener.onLocationChanged(lastLocation)
        Toast.makeText(mContext, "addLocationListener", Toast.LENGTH_SHORT).show()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun removeLocationListener() {
        mLocationManager?.removeUpdates(this.mLocationListener)
        mLocationManager = null
        Toast.makeText(mContext, "removeLocationListener", Toast.LENGTH_SHORT).show()
    }

    private inner class MyLocationListener : LocationListener {

        override fun onLocationChanged(p0: Location?) {
        }

        override fun onProviderEnabled(p0: String?) {
        }

        override fun onProviderDisabled(p0: String?) {
        }

        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
        }
    }
}