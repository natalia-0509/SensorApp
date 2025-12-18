package com.example.sensorapp

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.IOException
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.Future

class LocationActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
        private const val TAG = "LocationActivity"
    }

    private var lastLocation: Location? = null
    private lateinit var locationTextView: TextView
    private lateinit var addressTextView: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        locationTextView = findViewById(R.id.textview_location)
        addressTextView = findViewById(R.id.textview_address)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        findViewById<Button>(R.id.button_location).setOnClickListener { getLocation() }
        findViewById<Button>(R.id.button_address).setOnClickListener { executeGeocoding() }
    }

    private fun getLocation() {
        val granted = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                lastLocation = location
                locationTextView.text = getString(
                    R.string.location_text,
                    location.latitude,
                    location.longitude,
                    location.time
                )
            } else {
                locationTextView.text = getString(R.string.no_location)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation()
            } else {
                Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun locationGeocoding(location: Location): String {
        val geocoder = Geocoder(applicationContext, Locale.getDefault())
        var addresses: List<Address>? = null
        var resultMessage = ""

        try {
            addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        } catch (io: IOException) {
            resultMessage = getString(R.string.service_not_available)
            Log.e(TAG, resultMessage, io)
        }

        if (addresses.isNullOrEmpty()) {
            if (resultMessage.isEmpty()) {
                resultMessage = getString(R.string.no_address_found)
                Log.e(TAG, resultMessage)
            }
        } else {
            val address = addresses[0]
            val parts = ArrayList<String>()
            for (i in 0..address.maxAddressLineIndex) {
                parts.add(address.getAddressLine(i))
            }
            resultMessage = TextUtils.join("\n", parts)
        }

        return resultMessage
    }

    private fun executeGeocoding() {
        val loc = lastLocation ?: run {
            Toast.makeText(this, R.string.no_location, Toast.LENGTH_SHORT).show()
            return
        }

        val executor = Executors.newSingleThreadExecutor()
        val future: Future<String> = executor.submit<String> { locationGeocoding(loc) }

        try {
            val result = future.get()
            addressTextView.text = getString(R.string.address_text, result, System.currentTimeMillis())
        } catch (e: Exception) {
            Log.e(TAG, e.message ?: "Geocoding error", e)
            Thread.currentThread().interrupt()
        }
    }
}
