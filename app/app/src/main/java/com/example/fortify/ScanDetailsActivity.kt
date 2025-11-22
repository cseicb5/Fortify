package com.example.fortify

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class ScanDetailsActivity : AppCompatActivity() {

    // --- UI Views ---
    private lateinit var fileNameDetailsTextView: TextView
    private lateinit var detectionsRecyclerView: RecyclerView
    private lateinit var detailsProgressBar: ProgressBar
    private lateinit var noDetectionsTextView: TextView

    // --- Other variables ---
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var client: OkHttpClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_details)

        // Bind views and initialize components
        bindViews()
        client = OkHttpClient()
        sharedPreferences = getSharedPreferences("FortifyPrefs", Context.MODE_PRIVATE)

        // Get the Job ID and File Name passed from the previous screen
        val jobId = intent.getStringExtra("JOB_ID")
        val fileName = intent.getStringExtra("FILE_NAME")

        fileNameDetailsTextView.text = fileName

        if (jobId != null) {
            fetchScanDetails(jobId)
        } else {
            Toast.makeText(this, "Error: No Job ID provided.", Toast.LENGTH_LONG).show()
            detailsProgressBar.visibility = View.GONE
        }
    }

    private fun bindViews() {
        fileNameDetailsTextView = findViewById(R.id.fileNameDetailsTextView)
        detectionsRecyclerView = findViewById(R.id.detectionsRecyclerView)
        detailsProgressBar = findViewById(R.id.detailsProgressBar)
        noDetectionsTextView = findViewById(R.id.noDetectionsTextView)
    }

    private fun fetchScanDetails(jobId: String) {
        val serverUrl = sharedPreferences.getString("serverUrl", "")
        val jwtToken = sharedPreferences.getString("jwtToken", "") // We might need this for future security

        val request = Request.Builder()
            .url("$serverUrl/getScanDetails?jobID=$jobId")
            // .header("Authorization", "Bearer $jwtToken") // Optional: Add for extra security
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    detailsProgressBar.visibility = View.GONE
                    Toast.makeText(applicationContext, "Failed to load report: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()

                runOnUiThread {
                    detailsProgressBar.visibility = View.GONE
                    if (response.isSuccessful && responseBody != null) {
                        try {
                            val jsonResponse = JSONObject(responseBody)
                            val detectionsArray = jsonResponse.getJSONArray("detections")

                            if (detectionsArray.length() > 0) {
                                val detectionList = parseDetections(detectionsArray)
                                setupRecyclerView(detectionList)
                                detectionsRecyclerView.visibility = View.VISIBLE
                            } else {
                                // Show the "No threats detected" message
                                noDetectionsTextView.visibility = View.VISIBLE
                            }

                        } catch (e: Exception) {
                            Toast.makeText(applicationContext, "Failed to parse report.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(applicationContext, "Server error: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun parseDetections(jsonArray: JSONArray): List<DetectionItem> {
        val list = mutableListOf<DetectionItem>()
        for (i in 0 until jsonArray.length()) {
            val detectionObject = jsonArray.getJSONObject(i)
            list.add(
                DetectionItem(
                    file = detectionObject.getString("file"),
                    line = detectionObject.getInt("line"),
                    label = detectionObject.getString("label"),
                    match = detectionObject.getString("match")
                )
            )
        }
        return list
    }

    private fun setupRecyclerView(detections: List<DetectionItem>) {
        val adapter = DetectionAdapter(detections)
        detectionsRecyclerView.adapter = adapter
    }
}
