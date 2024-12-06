package com.example.foodsaverapps

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.foodsaverapps.databinding.ActivityMapsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.IOException
import java.util.*

class MapsActivity : AppCompatActivity() {

    private val FINE_PERMISSION_CODE = 1
    private val auth = FirebaseAuth.getInstance()
    private lateinit var mapView: MapView
    private lateinit var mapSearchView: SearchView
    private lateinit var setLocationButton: Button
    private lateinit var binding: ActivityMapsBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var userId: String
    private var currentLocation: Location? = null
    private var latestMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(applicationContext, androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext))

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mapView = findViewById(R.id.map)
        mapView.setMultiTouchControls(true)

        val myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), mapView)
        myLocationOverlay.enableMyLocation()
        myLocationOverlay.runOnFirstFix {
            currentLocation = myLocationOverlay.myLocationProvider.lastKnownLocation
            runOnUiThread {
                val myLocation = myLocationOverlay.myLocation
                if (myLocation != null) {
                    mapView.controller.setZoom(18.0)
                    mapView.controller.setCenter(GeoPoint(myLocation.latitude, myLocation.longitude))
                    addMarkerAtLocation(myLocation.latitude, myLocation.longitude)
                    // Disable follow location after setting the initial position
                    myLocationOverlay.disableFollowLocation()
                }
            }
        }
        mapView.overlays.add(myLocationOverlay)

        mapSearchView = findViewById(R.id.mapSearch)
        setLocationButton = findViewById(R.id.setLocation)
        userId = auth.currentUser?.uid.toString()

        mapSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    searchLocation(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        setLocationButton.setOnClickListener {
            setCurrentLocation()
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), FINE_PERMISSION_CODE)
        } else {
            myLocationOverlay.enableFollowLocation()
        }
    }

    private fun setCurrentLocation() {
        latestMarker?.let {
            val latitude = it.position.latitude
            val longitude = it.position.longitude
            saveLocationToFirebase(latitude, longitude)
        } ?: run {
            Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveLocationToFirebase(latitude: Double, longitude: Double) {
        val address = getAddressFromLatLng(latitude, longitude)
        val addressUrl = "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
        val database = FirebaseDatabase.getInstance()
        val userRef: DatabaseReference = database.reference.child("shop/$userId")
        userRef.child("address").setValue(address)
        userRef.child("addressUrl").setValue(addressUrl)
            .addOnSuccessListener {
                Toast.makeText(this, "Location and address saved", Toast.LENGTH_SHORT).show()
                val resultIntent = Intent()
                resultIntent.putExtra("address", address)
                setResult(Activity.RESULT_OK,resultIntent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save location and address", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getAddressFromLatLng(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        return if (addresses != null && addresses.isNotEmpty()) {
            val address = addresses[0]
           "${address.getAddressLine(0)}"
           // "${address.subAdminArea}"
        } else {
            "Address not found"
        }
    }

    private fun searchLocation(locationName: String) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocationName(locationName, 1)
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val address = addresses[0]
                    val geoPoint = GeoPoint(address.latitude, address.longitude)
                    mapView.controller.setCenter(geoPoint)
                    mapView.controller.setZoom(18.0)
                    addMarkerAtLocation(address.latitude, address.longitude)
                } else {
                    Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error in geocoding", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addMarkerAtLocation(latitude: Double, longitude: Double) {
        // Remove previous marker
        latestMarker?.let {
            mapView.overlays.remove(it)
        }

        val geoPoint = GeoPoint(latitude, longitude)
        val marker = Marker(mapView)
        marker.position = geoPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = getAddressFromLatLng(latitude, longitude)
        mapView.overlays.add(marker)
        mapView.invalidate()

        // Update latestMarker with the new marker
        latestMarker = marker
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), mapView)
                myLocationOverlay.enableMyLocation()
                mapView.overlays.add(myLocationOverlay)
                myLocationOverlay.enableFollowLocation()
            } else {
                Toast.makeText(this, "Location permission is not granted!", Toast.LENGTH_LONG).show()
            }
        }
    }
}
