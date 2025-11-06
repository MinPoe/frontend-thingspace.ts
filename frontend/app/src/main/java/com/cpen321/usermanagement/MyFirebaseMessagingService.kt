package com.cpen321.usermanagement

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            sendNotification(
                title = it.title ?: "New Notification",
                body = it.body ?: "",
                data = remoteMessage.data
            )
        }

        // Check if message contains a data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataPayload(remoteMessage.data)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM token: $token")
        
        // TODO: Send new token to your backend
        // This should be called from your ViewModel when user logs in
        sendTokenToServer(token)
    }

    private fun sendNotification(
        title: String,
        body: String,
        data: Map<String, String>
    ) {
        // Create intent to open app when notification is clicked
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            
            // Add workspace data for deep linking
            data["workspaceId"]?.let { putExtra("workspaceId", it) }
            data["workspaceName"]?.let { putExtra("workspaceName", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = "workspace_invites"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_heart_smile)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Workspace Invitations",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for workspace invitations"
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }

    private fun handleDataPayload(data: Map<String, String>) {
        val type = data["type"]
        val workspaceId = data["workspaceId"]
        val workspaceName = data["workspaceName"]
        
        Log.d(TAG, "Notification type: $type")
        Log.d(TAG, "Workspace ID: $workspaceId")
        Log.d(TAG, "Workspace Name: $workspaceName")
        
        // You can handle different notification types here
        when (type) {
            "workspace_invite" -> {
                // Could show a custom notification or update UI
                Log.d(TAG, "Received workspace invitation")
            }
        }
    }

    private fun sendTokenToServer(token: String) {
        // This will be implemented in your ViewModel
        // For now, just log it
        Log.d(TAG, "TODO: Send token to server: $token")
        
        // You'll implement this in AuthViewModel after login
        // Example: authRepository.updateFcmToken(token)
    }

    companion object {
        private const val TAG = "FCMService"
    }
}

