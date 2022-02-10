package fi.metatavu.pakkasmarja.services.erp.sap.session

import com.fasterxml.jackson.databind.ObjectMapper
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpHeaders
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Sap session controller
 */
@ApplicationScoped
class SapSessionController {
    @Inject
    @ConfigProperty(name="fi.metatavu.pakkasmarja.sap-api-url")
    lateinit var sapApiUrl: String

    @Inject
    @ConfigProperty(name="fi.metatavu.pakkasmarja.sap-company-db")
    lateinit var sapCompanyDb: String

    @Inject
    @ConfigProperty(name="fi.metatavu.pakkasmarja.sap-user-name")
    lateinit var sapUserName: String

    @Inject
    @ConfigProperty(name="fi.metatavu.pakkasmarja.sap-user-password")
    lateinit var sapUserPassword: String

    /**
     * Creates a new SAP session
     *
     * @return a new SAP session
     */
    fun createSapSession(): SapSession {
        try {
            val client = HttpClient.newHttpClient()
            val objectMapper = ObjectMapper()
            val loginInfo = objectMapper.createObjectNode()

            loginInfo.put("CompanyDB", sapCompanyDb)
            loginInfo.put("UserName", sapUserName)
            loginInfo.put("Password", sapUserPassword)
            val jsonString = objectMapper.writeValueAsString(loginInfo)
            val request = HttpRequest.newBuilder(URI("$sapApiUrl/Login")).POST(HttpRequest.BodyPublishers.ofString(jsonString)).build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofInputStream())
            when (val statusCode = response.statusCode()) {
                200 -> {
                    return parseLoginResponseHeaders(response.headers())
                }
                else -> {
                    throw SapSessionLoginException("Status code $statusCode from SAP")
                }
            }
        } catch (e: Exception) {
            val message = e.message
            throw SapSessionLoginException("Failed to start a SAP-session: $message")
        }

    }

    /**
     * Parses headers from a login response and creates a session using cookies from headers
     *
     * @param headers headers
     *
     * @return sap session
     */
    private fun parseLoginResponseHeaders(headers: HttpHeaders): SapSession {
        val cookies = headers.allValues("set-cookie") ?: throw SapSessionLoginException("'set-cookie' not found")
        val sessionCookie = cookies.findLast { cookie -> cookie.startsWith("B1SESSION") } ?: throw SapSessionLoginException("session cookie not found")
        val routeCookie = cookies.findLast { cookie -> cookie.startsWith("ROUTEID") } ?: throw SapSessionLoginException("" +
                "route cookie not found")

        val sessionId = parseIdFromCookie(sessionCookie)
        val routeId = parseIdFromCookie(routeCookie)

        return SapSession(apiUrl = sapApiUrl, sessionId = sessionId, routeId = routeId)
    }

    /**
     * Parses an id from a cookie
     *
     * @param cookie a cookie to parse
     *
     * @return id
     */
    private fun parseIdFromCookie(cookie: String): String {
        return cookie.split(";")[0].split("=")[1]
    }
}