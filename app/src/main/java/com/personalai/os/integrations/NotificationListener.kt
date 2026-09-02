package com.personalai.os.integrations

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * Read-only notification summarization (blueprint Part 11/12). The user
 * must manually enable this in system Settings > Notification access -
 * it is never auto-granted. This feeds the Unified Inbox's classification
 * pipeline; it does not send anything.
 */
class NotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // TODO: extract sbn.notification.extras (title/text), pass to
        // Communication Agent for classification. Never auto-act on it -
        // only summarize into the Unified Inbox for the user to review.
    }
}
