// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.codelabs.findnearbyplacesar

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.ar.sceneform.AnchorNode
import com.google.codelabs.findnearbyplacesar.api.NearbyPlacesResponse
import com.google.codelabs.findnearbyplacesar.api.PlacesService
import com.google.codelabs.findnearbyplacesar.ar.ArrowNode
import com.google.codelabs.findnearbyplacesar.ar.PlaceNode
import com.google.codelabs.findnearbyplacesar.ar.PlacesArFragment
import com.google.codelabs.findnearbyplacesar.model.Place
import com.google.codelabs.findnearbyplacesar.model.getPositionVector
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(), SensorEventListener {

    private val TAG = "MainActivity"
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private lateinit var placesService: PlacesService
    private lateinit var arFragment: PlacesArFragment
    private lateinit var mapFragment: SupportMapFragment

    // Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Sensor
    private lateinit var sensorManager: SensorManager
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var anchorNode: AnchorNode? = null
    private var markers: MutableList<Marker> = emptyList<Marker>().toMutableList()
    private var places: List<Place>? = null
    private var currentLocation: Location? = null
    private var map: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isSupportedDevice()) {
            return
        }
        setContentView(R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(R.id.ar_fragment) as PlacesArFragment
        mapFragment =
            supportFragmentManager.findFragmentById(R.id.maps_fragment) as SupportMapFragment

        sensorManager = getSystemService()!!
        placesService = PlacesService.create()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setUpAr()
        setUpMaps()
    }

    override fun onResume() {
        super.onResume()
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private fun setUpAr() {
        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            // Create anchor
            val anchor = hitResult.createAnchor()
            anchorNode = AnchorNode(anchor)
            anchorNode?.setParent(arFragment.arSceneView.scene)
            addPlaces(anchorNode!!)
        }
    }

    private fun addPlaces(anchorNode: AnchorNode) {
        val currentLocation = currentLocation
        if (currentLocation == null) {
            Log.w(TAG, "Location has not been determined yet")
            return
        }

        val places = places
        if (places == null) {
            Log.w(TAG, "No places to put")
            return
        }

        for (place in places) {
            // Add the place in AR
            val placeNode = PlaceNode(this, place)
            placeNode.setParent(anchorNode)
            placeNode.localPosition = place.getPositionVector(orientationAngles[0], currentLocation.latLng)
            placeNode.setOnTapListener { _, _ ->
                showInfoWindow(place)
            }

            // Add the place in maps
            map?.let {
                val marker = it.addMarker(
                    MarkerOptions()
                        .position(place.geometry.location.latLng)
                        .title(place.name)
                )
                marker.tag = place
                markers.add(marker)
            }
        }
    }

    private fun showInfoWindow(place: Place) {
        // Show in AR
        val matchingPlaceNode = anchorNode?.children?.filter {
            it is PlaceNode
        }?.first {
            val otherPlace = (it as PlaceNode).place ?: return@first false
            return@first otherPlace == place
        } as? PlaceNode
        matchingPlaceNode?.showInfoWindow()

        // Show as marker
        val matchingMarker = markers.firstOrNull {
            val placeTag = (it.tag as? Place) ?: return@firstOrNull false
            return@firstOrNull placeTag == place
        }
        matchingMarker?.showInfoWindow()
    }


    private fun setUpMaps() {
        mapFragment.getMapAsync { googleMap ->
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                googleMap.isMyLocationEnabled = true

                getCurrentLocation { location ->
                    val pos = CameraPosition.fromLatLngZoom(location.latLng, 13f)
                    googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos))
                    getNearbyPlaces(location)
                }
            } else {
                // 위치 권한이 없는 경우 사용자에게 권한을 요청합니다.
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            }

            googleMap.setOnMarkerClickListener { marker ->
                val tag = marker.tag
                if (tag is Place) {
                    // GeometryLocation을 Location으로 변환
                    val geometryLocation = tag.geometry.location
                    val destination = Location("").apply {
                        latitude = geometryLocation.lat
                        longitude = geometryLocation.lng
                    }
                    startARNavigation(destination)
                    return@setOnMarkerClickListener true
                }
                return@setOnMarkerClickListener false
            }
            map = googleMap
        }
    }
    // 안내 시작 메서드 구현
    private fun startARNavigation(destination: Location) {
        // 현재 위치가 없는 경우 처리
        val currentLocation = currentLocation ?: return

        // AR 화면에 화살표 추가
        val arrowNode = ArrowNode(arFragment, currentLocation.latLng, destination.latLng)
        arFragment.arSceneView.scene.addChild(arrowNode)

        // 화살표가 가리키는 방향으로 지도 카메라 이동
        val bearing = currentLocation.bearingTo(destination)
        val cameraPosition = CameraPosition.Builder()
            .target(destination.latLng)
            .bearing(bearing)
            .tilt(45f)
            .zoom(15f)
            .build()
        map?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 위치 권한이 허용된 경우 지도를 초기화합니다.
                setUpMaps()
            } else {
                // 위치 권한이 거부된 경우 사용자에게 메시지를 표시하거나 필요한 작업을 수행합니다.
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun getCurrentLocation(onSuccess: (Location) -> Unit) {
        // 위치 권한이 있는지 확인
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // 위치 권한이 있는 경우 위치 정보를 가져옴
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                currentLocation = location
                onSuccess(location)
            }.addOnFailureListener { exception ->
                Log.e(TAG, "Could not get location", exception)
            }
        } else {
            // 위치 권한이 없는 경우 사용자에게 권한을 요청
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }


    private fun getNearbyPlaces(location: Location) {
        val apiKey = this.getString(R.string.google_maps_key)
        placesService.nearbyPlaces(
            apiKey = apiKey,
            location = "${location.latitude},${location.longitude}",
            radiusInMeters = 2000,
            placeType = "park"
        ).enqueue(
            object : Callback<NearbyPlacesResponse> {
                override fun onFailure(call: Call<NearbyPlacesResponse>, t: Throwable) {
                    Log.e(TAG, "Failed to get nearby places", t)
                }

                override fun onResponse(
                    call: Call<NearbyPlacesResponse>,
                    response: Response<NearbyPlacesResponse>
                ) {
                    if (!response.isSuccessful) {
                        Log.e(TAG, "Failed to get nearby places")
                        return
                    }

                    val places = response.body()?.results ?: emptyList()
                    this@MainActivity.places = places
                }
            }
        )
    }


    private fun isSupportedDevice(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val openGlVersionString = activityManager.deviceConfigurationInfo.glEsVersion
        if (openGlVersionString.toDouble() < 3.0) {
            Toast.makeText(this, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                .show()
            finish()
            return false
        }
        return true
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) {
            return
        }
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }

        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )
        SensorManager.getOrientation(rotationMatrix, orientationAngles)
    }
}

val Location.latLng: LatLng
    get() = LatLng(this.latitude, this.longitude)

