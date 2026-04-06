package com.example.fortify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (messages.isEmpty()) return

            val senderNumber = messages[0].originatingAddress ?: "Unknown"
            val messageBody = StringBuilder()
            for (smsMessage in messages) {
                messageBody.append(smsMessage.messageBody)
            }
            val fullMessage = messageBody.toString()

            Log.d("SmsReceiver", "Intercepted SMS from $senderNumber. Sending to background service.")

            val serviceIntent = Intent(context, ScanMessageService::class.java).apply {
                putExtra("SMS_MESSAGE", fullMessage)
                putExtra("SENDER_NUMBER", senderNumber)
            }
            context.startService(serviceIntent)
        }
    }
}