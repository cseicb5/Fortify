package com.example.fortify // Or your package name

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.card.MaterialCardView

class HomeActivity : AppCompatActivity() {

    private lateinit var malwareScanCard: MaterialCardView
    private lateinit var phishingScanCard: MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        malwareScanCard = findViewById(R.id.malwareScanCard)
        phishingScanCard = findViewById(R.id.phishingScanCard)

        // --- THIS IS THE FIX ---
        // The click listener no longer opens a file picker.
        // It now directly starts the ScanApkActivity.
        malwareScanCard.setOnClickListener {
            val intent = Intent(this, ScanApkActivity::class.java)
            startActivity(intent)
        }
        // --- END OF FIX ---

        phishingScanCard.setOnClickListener {
            val intent = Intent(this, ScanMessageActivity::class.java)
            startActivity(intent)
        }
    }

    // The old, redundant file picker logic has been completely removed from this file.
}

