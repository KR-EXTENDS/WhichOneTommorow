package kr.e.whichonetommorow

import com.google.android.gms.maps.model.LatLng

interface TaskListener {
    /**
     * 緯度経度リスト取得完了時
     * @param list MutableList<LatLng>(失敗時はnull)
     */
    fun onComplete(list: MutableList<LatLng>?)
}