package com.example.fortify

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial

class HomeActivity : AppCompatActivity() {

    // --- UI Views ---
    private lateinit var malwareScanCard: MaterialCardView
    private lateinit var phishingScanCard: MaterialCardView
    private lateinit var backgroundScanSwitch: SwitchMaterial // <-- NEW

    // --- Other variables ---
    private lateinit var sharedPreferences: SharedPreferences // <-- NEW

    // This handles the result of the permission request dialog
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val smsGranted = permissions[Manifest.permission.RECEIVE_SMS] ?: false
            val notificationsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false
            } else {
                true // On older Android, this permission isn't needed
            }

            if (smsGranted && notificationsGranted) {
                Toast.makeText(this, "Background scanning is now active!", Toast.LENGTH_SHORT).show()
                // Save the state as enabled
                sharedPreferences.edit().putBoolean("backgroundScanEnabled", true).apply()
            } else {
                Toast.makeText(this, "Permissions are required for background scanning.", Toast.LENGTH_LONG).show()
                // If permissions are denied, turn the switch back off
                backgroundScanSwitch.isChecked = false
                sharedPreferences.edit().putBoolean("backgroundScanEnabled", false).apply()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("FortifyPrefs", Context.MODE_PRIVATE)

        // Bind all the views
        malwareScanCard = findViewById(R.id.malwareScanCard)
        phishingScanCard = findViewById(R.id.phishingScanCard)
        backgroundScanSwitch = findViewById(R.id.backgroundScanSwitch) // <-- NEW

        // Set up the click listeners for the cards
        malwareScanCard.setOnClickListener {
            startActivity(Intent(this, ScanApkActivity::class.java))
        }

        phishingScanCard.setOnClickListener {
            startActivity(Intent(this, ScanMessageActivity::class.java))
        }

        // --- NEW SWITCH LOGIC ---

        // 1. Set the initial state of the switch from saved preferences
        val isEnabled = sharedPreferences.getBoolean("backgroundScanEnabled", false)
        backgroundScanSwitch.isChecked = isEnabled

        // 2. Set a listener to react when the user flips the switch
        backgroundScanSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // If the user turns it ON, check for and request permissions
                checkAndRequestPermissions()
            } else {
                // If the user turns it OFF, save the state
                sharedPreferences.edit().putBoolean("backgroundScanEnabled", false).apply()
                Toast.makeText(this, "Background scanning disabled.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECEIVE_SMS)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            // Launch the permission request dialog
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            // If permissions are already granted, just enable the feature
            sharedPreferences.edit().putBoolean("backgroundScanEnabled", true).apply()
            Toast.makeText(this, "Background scanning is already enabled!", Toast.LENGTH_SHORT).show()
        }
    }
}

