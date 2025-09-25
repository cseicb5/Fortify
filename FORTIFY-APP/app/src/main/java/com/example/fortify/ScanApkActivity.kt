package com.example.fortify

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ScanApkActivity : AppCompatActivity() {

    // --- UI Views ---
    private lateinit var selectApkButton: Button
    private lateinit var scanJobsRecyclerView: RecyclerView

    // --- Other variables ---
    private lateinit var scanJobAdapter: ScanJobAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var client: OkHttpClient
    private val handler = Handler(Looper.getMainLooper())
    private val pollingRunnables = mutableMapOf<String, Runnable>()

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                uploadApkFile(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_apk)

        bindViews()
        client = OkHttpClient()
        sharedPreferences = getSharedPreferences("FortifyPrefs", Context.MODE_PRIVATE)

        scanJobAdapter = ScanJobAdapter(mutableListOf()) { clickedJob ->
            val intent = Intent(this, ScanDetailsActivity::class.java).apply {
                putExtra("JOB_ID", clickedJob.jobId)
                putExtra("FILE_NAME", clickedJob.fileName)
            }
            startActivity(intent)
        }

        scanJobsRecyclerView.adapter = scanJobAdapter

        selectApkButton.setOnClickListener {
            openFilePicker()
        }
    }

    private fun bindViews() {
        selectApkButton = findViewById(R.id.selectApkButton)
        scanJobsRecyclerView = findViewById(R.id.scanJobsRecyclerView)
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/vnd.android.package-archive"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        filePickerLauncher.launch(intent)
    }

    private fun uploadApkFile(uri: Uri) {
        val fileName = getFileName(uri) ?: "unknown.apk"
        val tempFile = File(cacheDir, fileName)

        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to read file.", Toast.LENGTH_SHORT).show()
            return
        }

        val placeholderJob = ScanJob(fileName, "-1", "Uploading...", R.color.card_background)
        scanJobAdapter.addJob(placeholderJob)

        val serverUrl = sharedPreferences.getString("serverUrl", "")
        val jwtToken = sharedPreferences.getString("jwtToken", "")

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", tempFile.name, tempFile.asRequestBody("application/vnd.android.package-archive".toMediaTypeOrNull()))
            .build()

        val request = Request.Builder()
            .url("$serverUrl/scanApk")
            .header("Authorization", "Bearer $jwtToken")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
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
                            scanJobAdapter.updateJobId("-1", jobId)
                            scanJobAdapter.updateJobStatus(jobId, "Scanning...", R.color.card_background)
                            startPolling(jobId)
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(applicationContext, "Failed to parse Job ID.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Server error: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun startPolling(jobId: String) {
        val serverUrl = sharedPreferences.getString("serverUrl", "")
        val jwtToken = sharedPreferences.getString("jwtToken", "")

        // --- THIS IS THE CORRECTED LOGIC ---
        // We declare the runnable variable here so it can refer to itself inside the block
        lateinit var runnable: Runnable
        runnable = Runnable {
            val request = Request.Builder()
                .url("$serverUrl/scanStatus?jobID=$jobId")
                .header("Authorization", "Bearer $jwtToken")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("PollingError", "Failed for job $jobId: ${e.message}")
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
                                val isMalicious = detectionResult.contains("Detections") && !detectionResult.startsWith("0")
                                val resultColor = if (isMalicious) R.color.result_malicious else R.color.result_clean
                                val finalStatus = if (isMalicious) detectionResult else "Clean"

                                runOnUiThread {
                                    scanJobAdapter.updateJobStatus(jobId, finalStatus, resultColor)
                                    pollingRunnables.remove(jobId)?.let { handler.removeCallbacks(it) }
                                }
                            } else {
                                // If not done, schedule the next poll using the variable name
                                handler.postDelayed(runnable, 10000)
                            }
                        } catch (e: Exception) {
                            handler.postDelayed(runnable, 10000)
                        }
                    } else {
                        handler.postDelayed(runnable, 10000)
                    }
                }
            })
        }
        // --- END OF CORRECTION ---

        pollingRunnables[jobId] = runnable
        handler.post(runnable)
    }

    private fun getFileName(uri: Uri): String? {
        var fileName: String? = null
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }
        return fileName
    }

    override fun onDestroy() {
        super.onDestroy()
        pollingRunnables.values.forEach { handler.removeCallbacks(it) }
    }
}

