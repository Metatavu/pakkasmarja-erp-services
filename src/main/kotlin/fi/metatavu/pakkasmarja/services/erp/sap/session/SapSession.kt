package fi.metatavu.pakkasmarja.services.erp.sap.session

import java.io.Closeable
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * Closeable Sap session
 *
 * @param apiUrl api url
 * @param routeId route id
 * @param sessionId session id
 */
class SapSession(val apiUrl: String, val routeId: String, val sessionId: String): AutoCloseable {
    /**
     * Closes a SAP session
     */
    override fun close() {
        val client = HttpClient.newHttpClient()
        val request = HttpRequest
            .newBuilder(URI.create("$apiUrl/Logout"))
            .setHeader("Cookie", "B1SESSION=$sessionId; ROUTEID=$routeId")
            .POST(HttpRequest.BodyPublishers.noBody())
            .build()

        try {
            val response = client.send(request, HttpResponse.BodyHandlers.ofInputStream())
            val statusCode = response.statusCode()
            if (statusCode != 204) {
                throw SapSessionLogoutException("Status code $statusCode from SAP")
            }
        } catch (e: Exception) {
            throw SapSessionLogoutException("Failed to stop a sap session: " + e.message)
        }

    }
}