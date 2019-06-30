package kr.e.whichonetommorow

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import kotlin.random.Random

object MainRepository {

    /** CSVから読み取った緯度経度のリスト */
    private var mLatLngList: MutableList<LatLng>? = null

    /**
     * CSVから読み取った緯度経度リストから、ランダムに一つ緯度経度を取得
     * @return LatLng
     */
    fun getRandomLatLng(context: Context): LatLng {
        if (null == mLatLngList) mLatLngList = readCsv(context)
        return this.mLatLngList!![Random.nextInt(mLatLngList!!.size)]
    }

    /**
     * CSVファイルを読み取り緯度経度リストを返却
     * @param context Context
     * @return 緯度経度リスト
     */
    private fun readCsv(context: Context): MutableList<LatLng> {
        val list: MutableList<LatLng> = mutableListOf()
        val csv = context.resources.assets.open("latlng.csv").reader(charset = Charsets.UTF_8).use { it.readText() }
        for (line in csv.split("\r\n")) {
            val tmp = line.split(",")
            list.add(LatLng(tmp[4].toDouble(), tmp[5].toDouble()))
        }
        return list
    }


}