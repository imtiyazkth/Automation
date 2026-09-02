package com.personalai.os.integrations.email

/**
 * Placeholder for the official Gmail API (OAuth) integration. Real
 * implementation should use Google's Android OAuth flow + the Gmail REST
 * API (users.messages.send) rather than raw SMTP credentials.
 */
class GmailClient {
    fun sendEmail(to: String, subject: String, body: String): String {
        // TODO: wire Google Sign-In + Gmail API (users.messages.send).
        return "[GmailClient not wired to OAuth yet]"
    }
}
