package com.example.fortify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.* // <--- NEW: Imported Coroutines

class ScanMessageService : Service() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var client: OkHttpClient
    private val handler = Handler(Looper.getMainLooper())

    // --- NEW: Create a background scope for Database operations ---
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        client = OkHttpClient()
        sharedPreferences = getSharedPreferences("FortifyPrefs", Context.MODE_PRIVATE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val message = intent?.getStringExtra("SMS_MESSAGE")
        val senderNumber = intent?.getStringExtra("SENDER_NUMBER")

        if (message != null && senderNumber != null) {
            scanMessage(message, senderNumber)
        } else {
            stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun scanMessage(message: String, senderNumber: String) {
        val serverUrl = sharedPreferences.getString("serverUrl", "")
        val jwtToken = sharedPreferences.getString("jwtToken", "")

        if (serverUrl.isNullOrEmpty() || jwtToken.isNullOrEmpty()) {
            Log.e("ScanService", "Server URL or Token not set. Cannot scan.")
            stopSelf()
            return
        }

        // Create a JSON object instead of a Form Body
        val jsonObject = JSONObject()
        jsonObject.put("message", message)

        // Define it explicitly as application/json
        val requestBody = jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url("$serverUrl/scanMessage")
            .header("Authorization", "Bearer $jwtToken")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ScanService", "Network failure on initial scan: ${e.message}")
                stopSelf()
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    try {
                        val jsonObject = JSONObject(responseBody)
                        val jobId = jsonObject.getString("jobID")

                        // Pass the original message down to the polling function
                        startPolling(jobId, senderNumber, message)

                    } catch (e: Exception) {
                        Log.e("ScanService", "Failed to parse job ID from response.")
                        stopSelf()
                    }
                } else {
                    Log.e("ScanService", "Server error on initial scan: ${response.message}")
                    stopSelf()
                }
            }
        })
    }

    // Polling function updated to match the new server integer status format
    private fun startPolling(jobId: String, senderNumber: String, message: String) {
        val serverUrl = sharedPreferences.getString("serverUrl", "")
        val jwtToken = sharedPreferences.getString("jwtToken", "")

        lateinit var pollingRunnable: Runnable
        pollingRunnable = Runnable {
            // 1. Create a JSON object with the jobID
            val jsonObject = JSONObject()
            jsonObject.put("jobID", jobId)

            // 2. Convert it to a JSON RequestBody
            val requestBody = jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

            // 3. Send a POST request to the clean URL
            val request = Request.Builder()
                .url("$serverUrl/scanStatus")
                .header("Authorization", "Bearer $jwtToken")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("ScanServicePolling", "Polling failed for job $jobId: ${e.message}")
                    handler.postDelayed(pollingRunnable, 5000)
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    if (response.isSuccessful && responseBody != null) {
                        try {
                            val json = JSONObject(responseBody)

                            // Check the new integer-based status from the server
                            if (json.has("status")) {
                                val status = json.getInt("status")

                                if (status == 1) {
                                    // Status 1 means DONE! Now we ask for the final results.
                                    fetchScanDetails(jobId, senderNumber, message)
                                    return // Exit the polling loop
                                } else if (status == -1) {
                                    Log.e("ScanService", "Server encountered an error processing the text.")
                                    stopSelf()
                                    return
                                }
                            }
                            // If status is 0, we just wait 5 seconds and poll again
                            handler.postDelayed(pollingRunnable, 5000)
                        } catch (e: Exception) {
                            Log.e("ScanServiceParse", "Failed to parse polling response for job $jobId")
                            handler.postDelayed(pollingRunnable, 5000)
                        }
                    } else {
                        // --- THE GHOST KILLER LOGIC ---
                        if (response.code == 404) {
                            Log.e("ScanService", "Server says job doesn't exist (404). Stopping service.")
                            stopSelf() // Kills the Android background loop!
                        } else {
                            handler.postDelayed(pollingRunnable, 5000)
                        }
                    }
                }
            })
        }
        handler.post(pollingRunnable)
    }

    // --- NEW FUNCTION TO GET THE ACTUAL RESULT ---
    private fun fetchScanDetails(jobId: String, senderNumber: String, message: String) {
        val serverUrl = sharedPreferences.getString("serverUrl", "")
        val jwtToken = sharedPreferences.getString("jwtToken", "")

        val jsonObject = JSONObject()
        jsonObject.put("jobID", jobId)
        val requestBody = jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url("$serverUrl/getScanDetails") // Hit the new route!
            .header("Authorization", "Bearer $jwtToken")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                stopSelf()
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    try {
                        val json = JSONObject(responseBody)
                        val detectionResult = json.getString("detection")

                        val isPhishing = detectionResult.equals("Phishing", ignoreCase = true)
                        val finalVerdict = if (isPhishing) "Phishing" else "Safe"

                        // --- NEW: SAVE TO ROOM DATABASE ---
                        serviceScope.launch {
                            try {
                                val db = AppDatabase.getDatabase(applicationContext)
                                db.messageDao().insertMessage(
                                    MessageEntity(
                                        jobId = jobId,
                                        sender = senderNumber,
                                        messageBody = message,
                                        result = finalVerdict
                                    )
                                )
                            } catch (e: Exception) {
                                Log.e("ScanService", "Error saving to Room Database", e)
                            }
                        }

                        // 1. Show Notification if malicious
                        if (isPhishing) {
                            handler.post { showPhishingNotification(senderNumber) }
                        }

                        // 2. Broadcast to HomeActivity Live Feed
                        val broadcastIntent = Intent("com.fortify.LIVE_UPDATE").apply {
                            putExtra("JOB_ID", jobId) // <--- ADD THIS LINE
                            putExtra("SENDER", senderNumber)
                            putExtra("BODY", message)
                            putExtra("RESULT", finalVerdict)
                        }
                        sendBroadcast(broadcastIntent)

                    } catch (e: Exception) {
                        Log.e("ScanService", "Failed to parse final details.")
                    }
                }
                stopSelf() // Always stop the background service when finished
            }
        })
    }

    private fun showPhishingNotification(senderNumber: String) {
        val channelId = "PHISHING_ALERT_CHANNEL"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Phishing Alerts", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for detected phishing attempts."
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationText = "A suspicious message was received from: $senderNumber"

        val notification = NotificationCompat.Builder(this, channelId)
            // Ensure this icon exists in your drawable folder!
            .setSmallIcon(R.drawable.ic_notification_shield)
            .setContentTitle("Phishing Attempt Detected!")
            .setContentText(notificationText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    // --- NEW: Clean up the Coroutines when the service dies ---
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}