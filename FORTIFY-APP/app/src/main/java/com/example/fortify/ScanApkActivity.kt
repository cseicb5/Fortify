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

    // This launcher handles the result from the file picker
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

        // Bind views and initialize components
        bindViews()
        client = OkHttpClient()
        sharedPreferences = getSharedPreferences("FortifyPrefs", Context.MODE_PRIVATE)

        // Setup the RecyclerView
        scanJobAdapter = ScanJobAdapter(mutableListOf())
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
        // --- THIS IS THE FIX ---
        // We now use the more general ACTION_GET_CONTENT.
        // This is more likely to show third-party file managers like Samsung's "My Files".
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/vnd.android.package-archive"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        // --- END OF FIX ---
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
                    val jsonObject = JSONObject(responseBody)
                    val jobId = jsonObject.getString("jobID")
                    runOnUiThread {
                        scanJobAdapter.updateJobId("-1", jobId)
                        scanJobAdapter.updateJobStatus(jobId, "Scanning...", R.color.card_background)
                        startPolling(jobId)
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

        val runnable = object : Runnable {
            override fun run() {
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
                            }
                        }
                    }
                })
                handler.postDelayed(this, 10000)
            }
        }
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

