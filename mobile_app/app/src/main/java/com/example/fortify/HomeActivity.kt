package com.example.fortify

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope // <--- NEW IMPORT
import kotlinx.coroutines.launch // <--- NEW IMPORT

class HomeActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private val messageList = mutableListOf<ScannedMessage>()

    // This listens for updates from your ScanMessageService
    private val liveUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val jobId = intent?.getStringExtra("JOB_ID") ?: ""
            val sender = intent?.getStringExtra("SENDER") ?: "Unknown"
            val body = intent?.getStringExtra("BODY") ?: ""
            val result = intent?.getStringExtra("RESULT") ?: "Error"

            // Add the new message to the top of the UI
            messageAdapter.addMessage(ScannedMessage(jobId, sender, body, result))
            recyclerView.scrollToPosition(0)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val smsGranted = permissions[Manifest.permission.RECEIVE_SMS] ?: false
            if (!smsGranted) {
                Toast.makeText(this, "Fortify cannot protect you without SMS permissions.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Setup the list UI structure
        recyclerView = findViewById(R.id.liveFeedRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        messageAdapter = MessageAdapter(messageList)
        recyclerView.adapter = messageAdapter

        // ==========================================
        // NEW: LOAD HISTORY FROM ROOM DATABASE
        // ==========================================
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            // Fetch old messages (the DAO is already ordered by timestamp)
            val oldMessages = db.messageDao().getAllMessages()

            messageList.clear() // Clear default list just in case

            // Convert database entities back into UI objects
            oldMessages.forEach { entity ->
                messageList.add(ScannedMessage(entity.jobId, entity.sender, entity.messageBody, entity.result))
            }

            // Tell the screen to redraw with the saved data
            messageAdapter.notifyDataSetChanged()
        }
        // ==========================================

        // Force request permissions on launch
        checkAndRequestPermissions()

        // Register the broadcast receiver so the UI updates live
        val filter = IntentFilter("com.fortify.LIVE_UPDATE")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(liveUpdateReceiver, filter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(liveUpdateReceiver, filter)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(liveUpdateReceiver)
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
        }
    }
}