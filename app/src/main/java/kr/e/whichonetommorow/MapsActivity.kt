package kr.e.whichonetommorow

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var mMapsViewModel: MapsViewModel
    private var mMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mMapsViewModel = ViewModelProviders.of(this).get(MapsViewModel::class.java)
        start_btn.setOnClickListener { mMapsViewModel.startRandomLatLng() }
        stop_btn.setOnClickListener {
            mMapsViewModel.stopRandomLatLng()
            // マーカーがあるところにカメラ移動
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMarker!!.position, mMapsViewModel.mZoomLevel))
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
        val latLngObserver: Observer<LatLng> = Observer{ t ->
            mMarker = mMap.addMarker(markerInit(t))
        }
        mMapsViewModel.mMarkerLatLng.observe(this, latLngObserver)
    }

    /**
     * MarkerOptions初期化
     * @param latLng 緯度経度
     * @return MarkerOptions
     */
    private fun markerInit(latLng: LatLng): MarkerOptions {
        mMarker?.remove()
        return MarkerOptions().apply {
            position(latLng)
            title("MARKER")
        }
    }

}
