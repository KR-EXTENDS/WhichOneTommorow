package kr.e.whichonetommorow

import com.google.android.gms.maps.model.LatLng

interface TaskListener {
    fun onComplete(list: MutableList<LatLng>?)
}