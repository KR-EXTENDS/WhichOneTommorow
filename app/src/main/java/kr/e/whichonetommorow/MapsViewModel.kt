package kr.e.whichonetommorow

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng
import java.util.*

/**
 * 地図表示に関するViewModel
 *
 * @param application
 */
class MapsViewModel(application: Application) : AndroidViewModel(Application()), LifecycleObserver {
    companion object {
        /** 緯度経度リストを更新(200ms秒ごと) */
        private const val UPDATE_SECOND = 200L
        /** マップ初期表示緯度：東京(北緯35.41)*/
        private const val LAT_DEFAULT: Double = 35.41
        /** マップ初期表示経度：東京(東経139.45)*/
        private const val LNG_DEFAULT: Double = 139.45
        /** マップ初期ズーム倍率：7.0 */
        private const val ZOOM_DEFAULT = 7.0f
        /** マーカー表示時ズーム倍率 */
        private const val ZOOM_MARKER = 10.0f
    }
    /** Map上マーカーの緯度経度 */
    var mMarkerLatLng = MutableLiveData<LatLng>()
        private set
    /** Map上のズームレベル保持(初期値：ZOOM_DEFAULT) */
    var mZoomLevel = ZOOM_DEFAULT
    /** Map上の緯度経度(初期値：東京) */
    var mLatLng = LatLng(LAT_DEFAULT, LNG_DEFAULT)
    /** 現在位置 */
    var mLocation = MutableLiveData<Location>()
    /** 経路情報 */
    var mRootInfo = MutableLiveData<MutableList<LatLng>>()
    private var mLocationManager: LocationManager? = null
    private val mLocationListener = MyLocationListener()
    private val mContext = application
    private var mApplication: Application = application
    private var mTimer: Timer? = null

    init {
        // CSVファイル初期読み取り
        MainRepository.getRandomLatLng(mApplication)
    }

    /**
     * 緯度経度をランダムに返却するタイマーイベントを開始
     * @return true：実行アリ
     */
    fun startRandomLatLng(): Boolean {
        // すでに開始されている場合はなにもしない
        if (null != mTimer) return false
        mTimer = Timer()
        mTimer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val newLatLng = MainRepository.getRandomLatLng(mApplication)
                mMarkerLatLng.postValue(newLatLng)
                mLatLng = newLatLng
            }
        }, UPDATE_SECOND, UPDATE_SECOND)
        return true
    }

    /**
     * 緯度経度をランダムに返却するタイマーイベントを停止
     * @return true：実行アリ
     */
    fun stopRandomLatLng(): Boolean {
        // すでに終了している場合はなにもしない
        if(null == mTimer) return false
        mTimer?.cancel()
        mTimer = null
        mZoomLevel = ZOOM_MARKER
        return true
    }

    /**
     * Google Directions API実行
     */
    fun doDirectionsApi() {
        MainRepository.doDirectionsApi(
            LatLng(mLocation.value!!.latitude, mLocation.value!!.longitude),
            mMarkerLatLng.value!!,
            mContext.getString(R.string.google_maps_key),
            object : TaskListener {
                override fun onComplete(list: MutableList<LatLng>?) {
                    Log.d("onComplete", "")
                    mRootInfo.postValue(list)
                }
            }
        )
    }

    /**
     * LocationListener開始
     */
    @SuppressLint("MissingPermission")
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun addLocationListener() {
        mLocationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mLocationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0F, mLocationListener)
        val lastLocation = mLocationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        this.mLocationListener.onLocationChanged(lastLocation)
    }

    /**
     * LocationListener停止
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun removeLocationListener() {
        mLocationManager?.removeUpdates(this.mLocationListener)
        mLocationManager = null
    }

    private inner class MyLocationListener : LocationListener {

        override fun onLocationChanged(p0: Location?) {
            Log.d("LocationViewModel", "lat=${p0?.latitude} lng=${p0?.longitude}")
            mLocation.value = p0
        }

        override fun onProviderEnabled(p0: String?) {
        }

        override fun onProviderDisabled(p0: String?) {
        }

        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
        }
    }
}