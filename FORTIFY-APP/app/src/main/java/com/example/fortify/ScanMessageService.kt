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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ScanMessageService : Service() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var client: OkHttpClient
    private val handler = Handler(Looper.getMainLooper())

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

        val requestBody = FormBody.Builder()
            .add("message", message)
            .build()

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
                        startPolling(jobId, senderNumber)
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

    private fun startPolling(jobId: String, senderNumber: String) {
        val serverUrl = sharedPreferences.getString("serverUrl", "")
        val jwtToken = sharedPreferences.getString("jwtToken", "")

        lateinit var pollingRunnable: Runnable
        pollingRunnable = Runnable {
            val request = Request.Builder()
                .url("$serverUrl/scanStatus?jobID=$jobId")
                .header("Authorization", "Bearer $jwtToken")
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
                            val details = json.getJSONObject("details")
                            val status = details.getString("STATUS")

                            if (status.equals("Done", ignoreCase = true)) {
                                val detectionResult = details.getString("DETECTION")
                                val isPhishing = detectionResult.contains("Phishing", ignoreCase = true) && !detectionResult.startsWith("Not", ignoreCase = true)

                                if (isPhishing) {
                                    // The call to the notification function no longer needs the result text
                                    handler.post { showPhishingNotification(senderNumber) }
                                }
                                stopSelf()
                            } else {
                                handler.postDelayed(pollingRunnable, 5000)
                            }
                        } catch (e: Exception) {
                            Log.e("ScanServiceParse", "Failed to parse polling response for job $jobId")
                            handler.postDelayed(pollingRunnable, 5000)
                        }
                    } else {
                        handler.postDelayed(pollingRunnable, 5000)
                    }
                }
            })
        }
        handler.post(pollingRunnable)
    }

    // --- THIS FUNCTION IS NOW UPDATED ---
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

        // --- THE NOTIFICATION TEXT IS NOW SIMPLER ---
        val notificationText = "A suspicious message was received from: $senderNumber"

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification_shield)
            .setContentTitle("Phishing Attempt Detected!")
            .setContentText(notificationText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}

