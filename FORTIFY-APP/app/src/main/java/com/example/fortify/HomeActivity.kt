package com.example.fortify // Or your package name

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts

class HomeActivity : AppCompatActivity() {

    private lateinit var scanApkButton: Button
    private lateinit var scanMessageButton: Button

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val fileName = getFileName(uri)
                val intent = Intent(this, ScanApkActivity::class.java).apply {
                    putExtra("FILE_URI", uri.toString())
                    putExtra("FILE_NAME", fileName)
                }
                startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        scanApkButton = findViewById(R.id.scanApkButton)
        scanMessageButton = findViewById(R.id.scanMessageButton)

        scanApkButton.setOnClickListener {
            openFilePicker()
        }

        scanMessageButton.setOnClickListener {
            // *** THIS IS THE CHANGE ***
            val intent = Intent(this, ScanMessageActivity::class.java)
            startActivity(intent)
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/vnd.android.package-archive"
        }
        filePickerLauncher.launch(intent)
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
}
