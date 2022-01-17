package com.example.actofitappdemo

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.actofitappdemo.databinding.ActivityMainBinding
import com.master.locationhelper.LocationHelper
import com.master.permissionhelper.PermissionHelper
import android.location.Geocoder
import java.util.*
import java.io.*

class MainActivity : AppCompatActivity() {

    val permissionHelper by lazy {
        PermissionHelper(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ),
            100
        )
    }
    lateinit var binding: ActivityMainBinding
    val locationHelper: LocationHelper by lazy { LocationHelper(this, this) }
    private val sharedPrefFile = "kotlinsharedpreference"
    private val locationPermissionCode = 2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationHelper.makeLifeCyclerAware(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        initView()
        startLocationUpdates()
    }

    private fun initView() {
        binding.btnLogout.setOnClickListener {
            val sharedpreferences = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
            val editor = sharedpreferences.edit()
            editor.clear()
            editor.apply()
            intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnStart.setOnClickListener {
            permissionHelper.requestAll {
                locationHelper.checkLocationSettings {
                    locationHelper.bindAndStartService()
                    binding.btnStart.visibility = View.INVISIBLE
                    binding.btnStop.visibility = View.VISIBLE
                }
            }
        }
        binding.btnStop.setOnClickListener {
            locationHelper.stopService()
            binding.btnStart.visibility = View.VISIBLE
            binding.btnStop.visibility = View.INVISIBLE
        }
        if (serviceIsRunningInForeground(this)) {
            binding.btnStart.visibility = View.INVISIBLE
            binding.btnStop.visibility = View.VISIBLE
        } else {
            binding.btnStart.visibility = View.VISIBLE
            binding.btnStop.visibility = View.INVISIBLE
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()

                locationHelper.checkLocationSettings {
                    locationHelper.fetchMultipleLocation { it ->
                        Log.e("LatLong", it.latitude.toString() + "   ==> " + it.longitude)
                        getFullAddress(it.latitude, it.longitude)
                    }
                }
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                startLocationUpdates()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        locationHelper.onActivityResult(requestCode, resultCode, data)
    }

    fun serviceIsRunningInForeground(context: Context): Boolean {
        val manager = context.getSystemService(
            Context.ACTIVITY_SERVICE
        ) as ActivityManager
        for (service in manager.getRunningServices(
            Integer.MAX_VALUE
        )) {
            Log.i("SERVICE", service.service.className)
            if (com.master.locationhelper.BackgroundLocationService::class.java.canonicalName == service.service.className) {
                if (service.foreground) {
                    return true
                }
            }
        }
        return false
    }

    private fun startLocationUpdates() {
        if ((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                locationPermissionCode
            )
        } else {
            locationHelper.checkLocationSettings {
                locationHelper.fetchMultipleLocation { it ->
                    Log.e("LatLong", it.latitude.toString() + "   ==> " + it.longitude)
                    getFullAddress(it.latitude, it.longitude)
                }
            }
        }
    }

    private fun getFullAddress(latitude: Double, longitude: Double) {
        val geocoder: Geocoder
        val addresses: List<Address>
        geocoder = Geocoder(this, Locale.getDefault())
        addresses = geocoder.getFromLocation(latitude, longitude, 1)
        val address: String = addresses[0].getAddressLine(0)
        /* val city: String = addresses[0].getLocality()
         val state: String = addresses[0].getAdminArea()
         val country: String = addresses[0].getCountryName()
         val postalCode: String = addresses[0].getPostalCode()
         val knownName: String = addresses[0].getFeatureName()*/
        Log.e("Address = >>>", address)
        binding.tvLocation.text = address
        generateNoteOnStorage(address)
    }

    private fun generateNoteOnStorage(address: String) {
        val path = this.getExternalFilesDir(null)
        val folder = File(path, "Location")
        folder.mkdirs()
        val file = File(folder, "CurrentLocation.txt")
        file.writeText("$address")
        Toast.makeText(
            this,
            "text File Saved And Updated At- Storage/Android/data/com.example.actofitappdemo/Location",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onResume() {
        super.onResume()
        //when use bind service will bind with activity, when activity closed then service will also closed
        //locationHelper.bindService()
    }

}