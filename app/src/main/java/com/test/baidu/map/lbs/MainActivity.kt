package com.test.baidu.map.lbs

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MapView
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.model.LatLng

class MainActivity : AppCompatActivity() {

    private var mMapView : MapView? = null
    private var mBaiduMap : BaiduMap? = null
    private var mLocationListener = MyLocationListener()
    private var mLocationClient : LocationClient? = null
    private var isFirstLocate : Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        //mMapView = findViewById<MapView>(R.id.bmapView)

        mMapView = MapView(this)
        setContentView(mMapView)

        mBaiduMap = mMapView?.map
        mBaiduMap?.isMyLocationEnabled = true

        mLocationClient = LocationClient(this)
        mLocationClient?.registerLocationListener(mLocationListener)

        var permissionList = ArrayList<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (permissionList.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionList.toTypedArray(), 1)
        } else {
            requestLocation()
        }
    }

    private fun requestLocation() {
        initLocation()
        mLocationClient?.start()
    }

    private fun initLocation() {
        var option = LocationClientOption()
        option.isOpenGps = true
        option.setCoorType("bd09ll")
        option.setScanSpan(10 * 1000)

        mLocationClient?.locOption = option
    }

    inner class MyLocationListener : BDAbstractLocationListener() {
        override fun onReceiveLocation(location: BDLocation?) {
            if (location == null || mMapView == null) {
                return
            }

            if (isFirstLocate) {
                var latLng = LatLng(location.latitude, location.longitude)

                mBaiduMap?.animateMapStatus(MapStatusUpdateFactory.newLatLng(latLng))
                mBaiduMap?.animateMapStatus(MapStatusUpdateFactory.zoomTo(19f))

                isFirstLocate = false
            }

            println("Latitude: " + location.latitude.toString())
            println("Longitude: " + location.longitude.toString())
            println("Radius: " + location.radius.toString())
            println("Direction: " + location.direction.toString())
            var locData : MyLocationData = MyLocationData.Builder()
                .latitude(location.latitude)
                .longitude(location.longitude).build()
            mBaiduMap?.setMyLocationData(locData)

            println("LocType: " + location.locType.toString())
        }

        override fun onLocDiagnosticMessage(locType: Int, diagnosticType: Int, diagnosticMessage: String?) {
            println("LocType: $locType")
            println("DiagnosticType: $diagnosticType")
            println("DiagnosticMessage: $diagnosticMessage")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            1 ->
                if (grantResults.isNotEmpty()) {
                    for (result in grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "Please confirm the consent to open the permission has been completed", Toast.LENGTH_SHORT).show()
                            finish()
                            return
                        }
                    }
                    requestLocation()
                } else {
                    Toast.makeText(this, "Unknown Error", Toast.LENGTH_SHORT).show()
                    finish()
                }
        }
    }

    override fun onResume() {
        mMapView?.onResume()
        super.onResume()
    }

    override fun onPause() {
        mMapView?.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mLocationClient?.stop()
        mBaiduMap?.isMyLocationEnabled = false
        mMapView?.onDestroy()
        mMapView = null
        super.onDestroy()
    }
}
