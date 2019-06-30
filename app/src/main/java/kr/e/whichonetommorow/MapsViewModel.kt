package kr.e.whichonetommorow

import android.app.Application
import android.content.Context
import android.location.LocationManager
import android.widget.Toast
import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng
import java.util.*

/**
 * 地図表示に関するViewModel
 *
 * @constructor
 * TODO
 *
 * @param application
 */
class MapsViewModel(application: Application) : AndroidViewModel(Application()) {
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
    private var mApplication: Application = application
    private var mTimer: Timer? = Timer()
    /** Map上マーカーの緯度経度 */
    var mMarkerLatLng = MutableLiveData<LatLng>()
        private set
    /** Map上のズームレベル保持(初期値：ZOOM_DEFAULT) */
    var mZoomLevel = ZOOM_DEFAULT
    /** Map上の緯度経度(初期値：東京) */
    var mLatLng = LatLng(LAT_DEFAULT, LNG_DEFAULT)

    init {
        // CSVファイル初期化
        MainRepository.getRandomLatLng(mApplication)
    }

    /**
     * 緯度経度をランダムに返却するタイマーイベントを開始
     */
    fun startRandomLatLng() {
        if (null == mTimer) mTimer = Timer()
        mTimer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val newLatLng = MainRepository.getRandomLatLng(mApplication)
                mMarkerLatLng.postValue(newLatLng)
                mLatLng = newLatLng
            }
        }, UPDATE_SECOND, UPDATE_SECOND)
    }

    /**
     * 緯度経度をランダムに返却するタイマーイベントを停止
     */
    fun stopRandomLatLng() {
        mTimer?.cancel()
        mTimer = null
        mZoomLevel = ZOOM_MARKER
    }
}