package com.example.fortify

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnticipateInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.animation.doOnEnd
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : AppCompatActivity() {

    private lateinit var serverUrlEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var proceedButton: Button
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var sharedPreferences: SharedPreferences

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val slideUp = ObjectAnimator.ofFloat(
                splashScreenView.iconView,
                View.TRANSLATION_Y,
                0f,
                -splashScreenView.iconView.height.toFloat()
            )
            slideUp.interpolator = AnticipateInterpolator()
            slideUp.duration = 800L
            slideUp.doOnEnd { splashScreenView.remove() }
            slideUp.start()
        }

        serverUrlEditText = findViewById(R.id.serverUrlEditText)
        usernameEditText = findViewById(R.id.usernameEditText)
        proceedButton = findViewById(R.id.proceedButton)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)

        sharedPreferences = getSharedPreferences("FortifyPrefs", Context.MODE_PRIVATE)

        val savedUrl = sharedPreferences.getString("serverUrl", "")
        if (!savedUrl.isNullOrEmpty()) {
            serverUrlEditText.setText(savedUrl)
        }

        proceedButton.setOnClickListener {
            var serverUrl = serverUrlEditText.text.toString().trim()
            val username = usernameEditText.text.toString().trim()

            if (serverUrl.isNotEmpty() && username.isNotEmpty()) {
                if (!serverUrl.startsWith("http://") && !serverUrl.startsWith("https://")) {
                    serverUrl = "http://$serverUrl"
                }
                setLoadingState(true)
                getTokenFromServer(serverUrl, username)
            } else {
                Toast.makeText(this, "Fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        proceedButton.isEnabled = !isLoading
        serverUrlEditText.isEnabled = !isLoading
        usernameEditText.isEnabled = !isLoading
    }

    private fun getTokenFromServer(serverUrl: String, username: String) {
        val jsonObject = JSONObject().apply { put("username", username) }
        val requestBody = jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url("$serverUrl/getToken")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    setLoadingState(false)
                    Toast.makeText(applicationContext, "Connection failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                runOnUiThread {
                    setLoadingState(false)
                    if (response.isSuccessful && responseBody != null) {
                        try {
                            val jsonResponse = JSONObject(responseBody)
                            val token = jsonResponse.getString("token")

                            sharedPreferences.edit().apply {
                                putString("serverUrl", serverUrl)
                                putString("username", username)
                                putString("jwtToken", token)
                            }.apply()

                            startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                            finish()
                        } catch (e: Exception) {
                            Toast.makeText(applicationContext, "Parsing error", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(applicationContext, "Server error: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}