package kr.e.whichonetommorow

import android.Manifest
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var isShowPermissionDialog: Boolean = false

    companion object {
        private const val REQUEST_LOCATION_PERMISSION_CODE = 1
    }

    private lateinit var mMap: GoogleMap
    private lateinit var mMapsViewModel: MapsViewModel
    private var mMarker: Marker? = null
    private var mCircle: Circle? = null
    private var mPolyline: Polyline? = null
    // TODO ProgressDialogは非推奨
    private var mProgressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        if (isLocationPermission()) {
            // パーミッションダイアログ表示(表示済みの場合はなにもしない)
            if (!isShowPermissionDialog) {
                ActivityCompat.requestPermissions(
                    this
                    , arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                    , REQUEST_LOCATION_PERMISSION_CODE
                )
            }
            isShowPermissionDialog = true
        } else {
            // 許可済み
            init()
        }
    }

    /**
     * 位置情報のパーミッションが下りているか判定
     * @return true：許可済
     */
    private fun isLocationPermission(): Boolean {
        return PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
                && PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION_CODE -> {
                isShowPermissionDialog = false
                if (grantResults.size == 2) {
                    if (PackageManager.PERMISSION_GRANTED == grantResults[0] && PackageManager.PERMISSION_GRANTED == grantResults[1]) {
                        init()
                    } else {
                        // パーミッションが下りなかった場合はアプリを終了する
                        finish()
                    }
                }
            }
        }
    }

    /**
     * 初期化
     */
    private fun init() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mMapsViewModel = ViewModelProviders.of(this).get(MapsViewModel::class.java)
        lifecycle.addObserver(mMapsViewModel)
        start_btn.setOnClickListener {
            mPolyline?.remove()
            mMapsViewModel.startRandomLatLng()
        }
        stop_btn.setOnClickListener {
            if (!mMapsViewModel.stopRandomLatLng()) return@setOnClickListener
            // マーカーがあるところにカメラ移動
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMarker!!.position, mMapsViewModel.mZoomLevel))
            // 経路情報検索
            if (null == mProgressDialog) mProgressDialog = ProgressDialog(this)
            mProgressDialog?.show()
            mMapsViewModel.doDirectionsApi()
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMapsViewModel.mLatLng, mMapsViewModel.mZoomLevel))
        subscribe()
        mMap.setOnCameraIdleListener {
            // 移動完了後、ViewModelで保持している緯度経度とズームレベルを変更
            val cameraPosition = mMap.cameraPosition
            mMapsViewModel.mLatLng = cameraPosition.target
            mMapsViewModel.mZoomLevel = cameraPosition.zoom
            Log.d("MapsActivity", "target=${cameraPosition.target} zoom=${cameraPosition.zoom}")
        }
    }

    /**
     * LiveData監視
     */
    private fun subscribe() {
        val latLngObserver: Observer<LatLng> = Observer { t ->
            // マーカーの位置を更新
            mMarker?.remove()
            mMarker = mMap.addMarker(markerInit(t))
        }
        val locationObserver: Observer<Location> = Observer { t ->
            // 現在地のサークル更新
            mCircle?.remove()
            mCircle = mMap.addCircle(circleInit(t))
        }
        val rootObserver: Observer<MutableList<LatLng>> = Observer { t ->
            // ルート情報更新
            mPolyline?.remove()
            mPolyline = mMap.addPolyline(PolylineOptions().apply {
                addAll(t)
            })
            mProgressDialog?.dismiss()
        }
        mMapsViewModel.mMarkerLatLng.observe(this, latLngObserver)
        mMapsViewModel.mLocation.observe(this, locationObserver)
        mMapsViewModel.mRootInfo.observe(this, rootObserver)
    }

    /**
     * MarkerOptions初期化
     * @param latLng 緯度経度
     * @return MarkerOptions
     */
    private fun markerInit(latLng: LatLng): MarkerOptions = MarkerOptions().apply {
        position(latLng)
        title("MARKER")
    }

    /**
     * CircleOptions初期化
     * @param location Location
     * @return CircleOptions
     */
    private fun circleInit(location: Location): CircleOptions = CircleOptions().apply {
        center(LatLng(location.latitude, location.longitude))
        // 描画円の半径 = 5.0m * (最大ズームレベル + 1.0 - 現在のズームレベル)
        radius(5.0 * (mMap.maxZoomLevel + 1.0f - mMap.cameraPosition.zoom))
        strokeColor(Color.BLUE)
        fillColor(Color.BLUE)
    }

}
