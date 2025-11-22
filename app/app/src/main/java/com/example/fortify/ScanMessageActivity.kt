package com.example.fortify // Or your package name

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ScanMessageActivity : AppCompatActivity() {

    // --- UI Views ---
    private lateinit var messageEditText: EditText
    private lateinit var scanMessageButton: Button
    private lateinit var resultCard: MaterialCardView
    private lateinit var resultCardLayout: ConstraintLayout
    private lateinit var pollingProgressBar: ProgressBar
    private lateinit var statusTitleTextView: TextView
    private lateinit var statusTextView: TextView

    // --- Other variables ---
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var client: OkHttpClient
    private val handler = Handler(Looper.getMainLooper())
    private var pollingRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_message)

        // Bind views and initialize components
        bindViews()
        client = OkHttpClient()
        sharedPreferences = getSharedPreferences("FortifyPrefs", Context.MODE_PRIVATE)

        scanMessageButton.setOnClickListener {
            val message = messageEditText.text.toString().trim()
            if (message.isNotEmpty()) {
                scanMessage(message)
            } else {
                Toast.makeText(this, "Please enter a message to scan.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bindViews() {
        // --- These are the NEW IDs from your redesigned layout ---
        messageEditText = findViewById(R.id.messageEditText)
        scanMessageButton = findViewById(R.id.scanMessageButton)
        resultCard = findViewById(R.id.resultCard)
        resultCardLayout = findViewById(R.id.resultCardLayout)
        pollingProgressBar = findViewById(R.id.pollingProgressBar)
        statusTitleTextView = findViewById(R.id.statusTitleTextView)
        statusTextView = findViewById(R.id.statusTextView)
    }

    private fun setUiState(isScanning: Boolean) {
        scanMessageButton.isEnabled = !isScanning
        messageEditText.isEnabled = !isScanning
        resultCard.visibility = if (isScanning) View.VISIBLE else View.GONE
        pollingProgressBar.visibility = if (isScanning) View.VISIBLE else View.GONE
        statusTitleTextView.visibility = if (isScanning) View.VISIBLE else View.GONE
        statusTextView.visibility = if (isScanning) View.VISIBLE else View.GONE

        if (isScanning) {
            statusTextView.text = "Scanning..."
            resultCardLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.card_background))
        }
    }

    private fun scanMessage(message: String) {
        setUiState(true)

        // Get server details
        val serverUrl = sharedPreferences.getString("serverUrl", "")
        val jwtToken = sharedPreferences.getString("jwtToken", "")

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
                runOnUiThread {
                    setUiState(false)
                    Toast.makeText(applicationContext, "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    try {
                        val jsonObject = JSONObject(responseBody)
                        val jobId = jsonObject.getString("jobID")
                        runOnUiThread {
                            startPolling(jobId)
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            setUiState(false)
                            Toast.makeText(applicationContext, "Failed to parse job ID.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        setUiState(false)
                        Toast.makeText(applicationContext, "Server error on upload: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun startPolling(jobId: String) {
        val serverUrl = sharedPreferences.getString("serverUrl", "")
        val jwtToken = sharedPreferences.getString("jwtToken", "")

        pollingRunnable = object : Runnable {
            override fun run() {
                val request = Request.Builder()
                    .url("$serverUrl/scanStatus?jobID=$jobId")
                    .header("Authorization", "Bearer $jwtToken")
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e("PollingError", "Failed to get status: ${e.message}")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responseBody = response.body?.string()
                        if (response.isSuccessful && responseBody != null) {
                            try {
                                val json = JSONObject(responseBody)
                                val details = json.getJSONObject("details")
                                val status = details.getString("STATUS")

                                if (status.equals("Done", ignoreCase = true)) {
                                    handler.removeCallbacks(pollingRunnable!!)
                                    val detectionResult = details.getString("DETECTION")
                                    runOnUiThread {
                                        displayResults(detectionResult)
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("PollingParseError", "Failed to parse status response.")
                            }
                        }
                    }
                })
                handler.postDelayed(this, 5000) // Poll every 5 seconds
            }
        }
        handler.post(pollingRunnable!!)
    }

    private fun displayResults(result: String) {
        pollingProgressBar.visibility = View.GONE
        statusTitleTextView.visibility = View.VISIBLE
        statusTextView.visibility = View.VISIBLE
        scanMessageButton.isEnabled = true
        messageEditText.isEnabled = true

        val isPhishing = result.contains("Phishing", ignoreCase = true) && !result.startsWith("Not", ignoreCase = true)

        if (isPhishing) {
            statusTextView.text = result
            resultCardLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.result_malicious))
        } else {
            statusTextView.text = "Not phishing"
            resultCardLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.result_clean))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        pollingRunnable?.let { handler.removeCallbacks(it) }
    }
}

