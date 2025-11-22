package com.example.fortify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val sharedPreferences = context.getSharedPreferences("FortifyPrefs", Context.MODE_PRIVATE)
        val isEnabled = sharedPreferences.getBoolean("backgroundScanEnabled", false)

        if (isEnabled && intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (messages.isEmpty()) {
                return // No messages to process
            }

            // --- THIS IS THE NEW CODE ---
            // Get the sender's phone number from the first message part.
            // All parts of a single SMS will have the same sender.
            val senderNumber = messages[0].originatingAddress
            // --- END OF NEW CODE ---

            val messageBody = StringBuilder()
            for (smsMessage in messages) {
                messageBody.append(smsMessage.messageBody)
            }
            val fullMessage = messageBody.toString()

            Log.d("SmsReceiver", "SMS from $senderNumber. Starting scan service.")

            // Start the background service, now passing BOTH the message and the sender number
            val serviceIntent = Intent(context, ScanMessageService::class.java).apply {
                putExtra("SMS_MESSAGE", fullMessage)
                putExtra("SENDER_NUMBER", senderNumber) // <-- NEW
            }
            context.startService(serviceIntent)
        } else {
            Log.d("SmsReceiver", "Feature disabled or intent is not an SMS. Ignoring.")
        }
    }
}

