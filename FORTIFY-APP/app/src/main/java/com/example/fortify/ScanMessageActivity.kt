package com.example.fortify // Or your package name

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ScanMessageActivity : AppCompatActivity() {

    // UI Views
    private lateinit var messageEditText: EditText
    private lateinit var scanMessageButton: Button
    private lateinit var scanProgressBar: ProgressBar
    private lateinit var statusTextView: TextView
    private lateinit var resultsCardView: CardView
    private lateinit var phishingResultTextView: TextView

    // Networking and Data
    private val client = OkHttpClient()
    private lateinit var serverUrl: String
    private lateinit var jwtToken: String

    // Polling
    private val handler = Handler(Looper.getMainLooper())
    private var pollingRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_message)

        bindViews()

        val sharedPreferences = getSharedPreferences("FortifyPrefs", Context.MODE_PRIVATE)
        serverUrl = sharedPreferences.getString("serverUrl", "")!!
        jwtToken = sharedPreferences.getString("jwtToken", "")!!

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
        messageEditText = findViewById(R.id.messageEditText)
        scanMessageButton = findViewById(R.id.scanMessageButton)
        scanProgressBar = findViewById(R.id.scanProgressBar)
        statusTextView = findViewById(R.id.statusTextView)
        resultsCardView = findViewById(R.id.resultsCardView)
        phishingResultTextView = findViewById(R.id.phishingResultTextView)
    }

    private fun setUiState(isScanning: Boolean) {
        scanMessageButton.isEnabled = !isScanning
        messageEditText.isEnabled = !isScanning
        scanProgressBar.visibility = if (isScanning) View.VISIBLE else View.GONE
        statusTextView.visibility = if (isScanning) View.VISIBLE else View.GONE
        if (isScanning) {
            statusTextView.text = "Status: Scanning..."
            resultsCardView.visibility = View.GONE
        }
    }

    private fun scanMessage(message: String) {
        setUiState(true)

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
                    val jsonObject = JSONObject(responseBody)
                    val jobId = jsonObject.getString("jobID")
                    runOnUiThread {
                        startPolling(jobId)
                    }
                } else {
                    runOnUiThread {
                        setUiState(false)
                        Toast.makeText(applicationContext, "Server error: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun startPolling(jobId: String) {
        pollingRunnable = object : Runnable {
            override fun run() {
                checkScanStatus(jobId)
                handler.postDelayed(this, 5000) // Poll every 5 seconds
            }
        }
        handler.post(pollingRunnable!!)
    }

    private fun checkScanStatus(jobId: String) {
        val request = Request.Builder()
            .url("$serverUrl/scanStatus?jobID=$jobId")
            .header("Authorization", "Bearer $jwtToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    val json = JSONObject(responseBody)
                    val details = json.getJSONObject("details")
                    val status = details.getString("STATUS")

                    if (status.equals("Done", ignoreCase = true)) {
                        handler.removeCallbacks(pollingRunnable!!)
                        val result = details.getString("DETECTION")
                        runOnUiThread {
                            setUiState(false)
                            displayResult(result)
                        }
                    }
                }
            }
        })
    }

    private fun displayResult(result: String) {
        resultsCardView.visibility = View.VISIBLE
        phishingResultTextView.text = "Result: $result"
    }

    override fun onDestroy() {
        super.onDestroy()
        pollingRunnable?.let { handler.removeCallbacks(it) }
    }
}
