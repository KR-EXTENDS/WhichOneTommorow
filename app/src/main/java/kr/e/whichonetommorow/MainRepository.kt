package kr.e.whichonetommorow

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
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

    /**
     * GoogleDirectionsApi実行
     *
     * @param origin 出発地
     * @param destination 目的地
     * @param key APIキー
     * @param taskListener 非同期処理完了後リスナー
     */
    fun doDirectionsApi(origin: LatLng, destination: LatLng, key: String, taskListener: TaskListener) {
        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${origin.latitude},${origin.longitude}&" +
                "destination=${destination.latitude},${destination.longitude}&" +
                "mode=walking&" +
                "key=$key"
        MyTask(taskListener).execute(url)
    }

    private class MyTask(taskListener: TaskListener) : AsyncTask<String, Void, String>() {

        val mTaskListener = taskListener
        val mResultList= mutableListOf<LatLng>()

        override fun doInBackground(vararg p0: String?): String {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(p0[0])
                .build()
            Log.d("MyTask#doInBackground", "url=${p0[0]}")
            val response = client.newCall(request).execute()
            val body = response.body()?.string()
            Log.d("MyTask#doInBackground", body)
            val resJSON = JSONObject(body)
            val status = resJSON.get("status").toString()
            if (status != "OK") return status
            val steps = resJSON
                .getJSONArray("routes").getJSONObject(0)
                .getJSONArray("legs").getJSONObject(0)
                .getJSONArray("steps")
            for (i in 0 until steps.length()) {
                val location = steps.getJSONObject(i).getJSONObject("start_location")
                val lat = location.get("lat").toString().toDouble()
                val lng = location.get("lng").toString().toDouble()
                mResultList.add(LatLng(lat, lng))
            }
            return status
        }

        override fun onPostExecute(result: String?) {
            Log.d("MyTask#doInBackground", "onPostExecute status=$result")
            mTaskListener.onComplete(mResultList)
        }

    }


}