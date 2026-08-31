package com.arrowescape.game.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Data payload for withdrawal admin notifications.
 */
data class WithdrawalNotificationPayload(
    val withdrawalId: String,
    val userId: String,
    val username: String,
    val amount: Double,
    val maskedUpiId: String,
    val timestamp: Long,
    val status: String = "SUCCESSFUL"
) {
    fun toFormattedTelegramMessage(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateStr = sdf.format(Date(timestamp))
        return buildString {
            append("🔔 *Withdrawal Successful*\n")
            append("• *Withdrawal ID:* `$withdrawalId`\n")
            append("• *User ID:* `$userId` ($username)\n")
            append("• *Amount:* ₹${"%.2f".format(amount)}\n")
            append("• *UPI ID:* `$maskedUpiId`\n")
            append("• *Status:* $status\n")
            append("• *Timestamp:* $dateStr")
        }
    }
}

sealed class NotificationResult {
    object Success : NotificationResult()
    data class Skipped(val reason: String) : NotificationResult()
    data class Failed(val error: String) : NotificationResult()
}

/**
 * Clean service abstraction for admin notifications.
 * SECURITY RULE:
 * Private credentials, Telegram bot tokens, and WhatsApp API keys must NEVER be bundled inside the APK.
 * All notification dispatches route through secure server-side webhook endpoints.
 */
interface WithdrawalNotificationService {
    suspend fun notifyWithdrawalSuccessful(payload: WithdrawalNotificationPayload): NotificationResult
    fun isTelegramBackendConfigured(): Boolean
    fun isWhatsAppBackendConfigured(): Boolean
}

object DefaultWithdrawalNotificationService : WithdrawalNotificationService {
    private const val TAG = "WithdrawalNotification"

    // Server-side webhook proxy URL can be injected at runtime or configured via secure environment
    @Volatile
    var serverWebhookUrl: String? = null

    override fun isTelegramBackendConfigured(): Boolean {
        return !serverWebhookUrl.isNullOrBlank()
    }

    override fun isWhatsAppBackendConfigured(): Boolean {
        return false // WhatsApp Business API backend not provisioned by default
    }

    override suspend fun notifyWithdrawalSuccessful(payload: WithdrawalNotificationPayload): NotificationResult {
        return withContext(Dispatchers.IO) {
            val webhook = serverWebhookUrl
            if (webhook.isNullOrBlank()) {
                Log.d(
                    TAG,
                    "Notification skipped: Server webhook endpoint not configured. " +
                            "Telegram/WhatsApp bot credentials must reside on the backend server for security.\n" +
                            payload.toFormattedTelegramMessage()
                )
                return@withContext NotificationResult.Skipped(
                    "Server webhook endpoint not configured. Real bot credentials reside on the backend."
                )
            }

            try {
                val url = URL(webhook)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; utf-8")
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true
                connection.connectTimeout = 8000
                connection.readTimeout = 8000

                val jsonBody = """
                    {
                        "event": "WITHDRAWAL_SUCCESSFUL",
                        "withdrawalId": "${payload.withdrawalId}",
                        "userId": "${payload.userId}",
                        "username": "${payload.username}",
                        "amount": ${payload.amount},
                        "maskedUpiId": "${payload.maskedUpiId}",
                        "timestamp": ${payload.timestamp},
                        "status": "${payload.status}"
                    }
                """.trimIndent()

                connection.outputStream.use { os ->
                    val input = jsonBody.toByteArray(Charsets.UTF_8)
                    os.write(input, 0, input.size)
                }

                val responseCode = connection.responseCode
                if (responseCode in 200..299) {
                    Log.d(TAG, "Successfully dispatched withdrawal notification to backend for ID: ${payload.withdrawalId}")
                    NotificationResult.Success
                } else {
                    val errorMsg = "Backend returned HTTP $responseCode"
                    Log.w(TAG, "Notification failed: $errorMsg")
                    NotificationResult.Failed(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to dispatch notification: ${e.message}", e)
                NotificationResult.Failed(e.message ?: "Network error during notification dispatch")
            }
        }
    }
}
