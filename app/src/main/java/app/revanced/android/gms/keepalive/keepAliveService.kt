package app.revanced.android.gms.keepalive

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class KeepAliveService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        pingMicroG()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No event processing needed
    }

    override fun onInterrupt() {
        // Required method override
    }

    private fun pingMicroG() {
        try {
            // Wake microG's background service when accessibility connects
            val intent = Intent().apply {
                component = ComponentName(
                    "app.revanced.android.gms",
                    "org.microg.gms.gcm.MqttService"
                )
                action = "org.microg.gms.gcm.CHECKIN"
            }
            sendBroadcast(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}