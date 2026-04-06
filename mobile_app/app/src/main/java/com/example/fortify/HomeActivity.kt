package com.example.fortify

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HomeActivity : AppCompatActivity() {

    private lateinit var malwareScanCard: MaterialCardView
    private lateinit var phishingScanCard: MaterialCardView
    private lateinit var backgroundScanSwitch: SwitchMaterial
    private lateinit var sharedPreferences: SharedPreferences

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val smsGranted = permissions[Manifest.permission.RECEIVE_SMS] ?: false
            val notificationsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false
            } else {
                true
            }

            if (smsGranted && notificationsGranted) {
                Toast.makeText(this, "Background Sentinel Active", Toast.LENGTH_SHORT).show()
                sharedPreferences.edit().putBoolean("backgroundScanEnabled", true).apply()
            } else {
                Toast.makeText(this, "Permissions required for monitoring.", Toast.LENGTH_LONG).show()
                backgroundScanSwitch.isChecked = false
                sharedPreferences.edit().putBoolean("backgroundScanEnabled", false).apply()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        sharedPreferences = getSharedPreferences("FortifyPrefs", Context.MODE_PRIVATE)

        malwareScanCard = findViewById(R.id.malwareScanCard)
        phishingScanCard = findViewById(R.id.phishingScanCard)
        backgroundScanSwitch = findViewById(R.id.backgroundScanSwitch)

        // --- UI CLEANUP ---
        // Hiding APK scanning UI to focus on Phishing Sentinel
        malwareScanCard.visibility = View.GONE

        phishingScanCard.setOnClickListener {
            startActivity(Intent(this, ScanMessageActivity::class.java))
        }

        // --- SWITCH LOGIC WITH CONFIRMATION ---
        val isEnabled = sharedPreferences.getBoolean("backgroundScanEnabled", false)
        backgroundScanSwitch.isChecked = isEnabled

        backgroundScanSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // Only show dialog if the change was triggered by a user touch
                if (buttonView.isPressed) {
                    showBackgroundConfirmation()
                }
            } else {
                sharedPreferences.edit().putBoolean("backgroundScanEnabled", false).apply()
                Toast.makeText(this, "Background monitoring disabled.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showBackgroundConfirmation() {
        MaterialAlertDialogBuilder(this, R.style.FortifyAlertDialog)
            .setTitle("Enable Background Sentinel?")
            .setMessage("Fortify will scan incoming SMS for malicious links in the background. Do you wish to enable this protection?")
            .setCancelable(false)
            .setPositiveButton("Enable") { _, _ ->
                checkAndRequestPermissions()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                backgroundScanSwitch.isChecked = false
                dialog.dismiss()
            }
            .show()
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
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            sharedPreferences.edit().putBoolean("backgroundScanEnabled", true).apply()
            Toast.makeText(this, "Background Sentinel Active", Toast.LENGTH_SHORT).show()
        }
    }
}