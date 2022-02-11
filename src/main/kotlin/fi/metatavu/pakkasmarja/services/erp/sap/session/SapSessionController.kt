package fi.metatavu.pakkasmarja.services.erp.sap.session

import com.fasterxml.jackson.databind.ObjectMapper
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.net.HttpCookie
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
            val jsonBytes = objectMapper.writeValueAsBytes(loginInfo)
            val request = HttpRequest.newBuilder(URI("$sapApiUrl/Login")).POST(HttpRequest.BodyPublishers.ofByteArray(jsonBytes)).build()

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
        val cookies = headers.allValues("set-cookie").map(HttpCookie::parse)
        val sessionId = cookies.find { cookie -> cookie[0].name == "B1SESSION" }?.get(0)?.value ?: throw SapSessionLoginException("session cookie not found")
        val routeId = cookies.find { cookie -> cookie[0].name == "ROUTEID" }?.get(0)?.value ?: throw SapSessionLoginException("route cookie not found")

        return SapSession(apiUrl = sapApiUrl, sessionId = sessionId, routeId = routeId)
    }
}