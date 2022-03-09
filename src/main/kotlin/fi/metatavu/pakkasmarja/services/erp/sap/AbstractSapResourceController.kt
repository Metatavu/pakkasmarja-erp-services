package fi.metatavu.pakkasmarja.services.erp.sap

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.pakkasmarja.services.erp.sap.exception.SapCountFetchException
import fi.metatavu.pakkasmarja.services.erp.sap.exception.SapItemFetchException
import fi.metatavu.pakkasmarja.services.erp.sap.exception.SapModificationException
import fi.metatavu.pakkasmarja.services.erp.sap.session.SapSession
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.OffsetDateTime
import javax.enterprise.context.ApplicationScoped

/**
 * Abstract SAP-resource controller
 */
@ApplicationScoped
abstract class AbstractSapResourceController {

    /**
     * Creates an item to SAP
     *
     * @param item item to create
     * @param resourceUrl resource url
     * @param sessionId SAP session id
     * @param routeId SAP session route id
     * @return created item
     */
    fun createItem(
        item: JsonNode,
        resourceUrl: String,
        sessionId: String,
        routeId: String
    ): JsonNode {
        try {
            return sendSapPostOrPatchRequest(
                item = item,
                resourceUrl = resourceUrl,
                sessionId = sessionId,
                routeId = routeId,
                method = "POST"
            )
        } catch (e: Exception) {
            throw SapModificationException("Failed to create an item to SAP: ${e.message}")
        }
    }

    /**
     * Finds item from SAP
     *
     * @param itemUrl item url
     * @param sessionId SAP-session id
     * @param routeId SAP-session route id
     * @return found item or null
     */
    fun findItem(itemUrl: String, sessionId: String, routeId: String): JsonNode? {
        try {
            val client = HttpClient.newHttpClient()

            val request = HttpRequest
                .newBuilder(URI.create(itemUrl))
                .setHeader("Cookie", "B1SESSION=$sessionId; ROUTEID=$routeId")
                .GET()
                .build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofByteArray())
            if (response.statusCode() != 200) {
                return null
            }

            return ObjectMapper().readTree(response.body())
        } catch (e: Exception) {
            throw SapItemFetchException("Failed to fetch items from SAP: ${e.message}")
        }
    }

    /**
     * Updates an item to SAP
     *
     * @param item item to update
     * @param resourceUrl resource url
     * @param sessionId SAP session id
     * @param routeId SAP session route id
     * @return updated item
     */
    fun updateItem(
        item: JsonNode,
        resourceUrl: String,
        sessionId: String,
        routeId: String
    ): JsonNode {
        try {
            return sendSapPostOrPatchRequest(
                item = item,
                resourceUrl = resourceUrl,
                sessionId = sessionId,
                routeId = routeId,
                method = "PATCH"
            )
        } catch (e: Exception) {
            throw SapModificationException("Failed to update an item to SAP: ${e.message}")
        }
    }

    /**
     * Creates an "updatedAfter"-filter from an OffsetDateTime-object
     *
     * @param updatedAfter a value to be used for the filter
     * @return "updatedAfter"-filter
     */
    fun createdUpdatedAfterFilter (updatedAfter: OffsetDateTime): String {
        val updateDate = updatedAfter.toLocalDate().toString()
        val updateTime = updatedAfter.toLocalTime().toString().split(".")[0]
        return "(UpdateDate gt '$updateDate' or (UpdateDate eq '$updateDate' and UpdateTime gt '$updateTime'))"
    }

    /**
     * Constructs SAP request URL
     *
     * @param baseUrl base URL
     * @param select select string
     * @param filter filter string
     * @return list of SAP request URL
     */
    fun constructSAPRequestUrl(
        baseUrl: String,
        select: String,
        filter: String?,
        firstResult: Int?
    ): String {
        val requestUrl = if (filter != null) {
            "$baseUrl?$select&${escapeSapQuery(filter)}"
        } else {
            "$baseUrl?$select"
        }

        return if (firstResult != null) {
            "$requestUrl&\$skip=$firstResult"
        } else {
            requestUrl
        }
    }


    /**
     * Fetches items from multiple urls and combines them into a single list
     *
     * @param requestUrl list SAP request URL's
     * @param sapSession SAP-session
     * @param maxResults max results, default is 9999
     * @return list of items
     */
    fun getItemsRequest(requestUrl: String, sapSession: SapSession, maxResults: Int? = 9999): List<JsonNode> {
        try {
            val client = HttpClient.newHttpClient()

            val request = HttpRequest
                .newBuilder(URI.create(requestUrl))
                .setHeader("Cookie", "B1SESSION=${sapSession.sessionId}; ROUTEID=${sapSession.routeId}")
                .setHeader("Prefer","odata.maxpagesize=${maxResults}")
                .GET()
                .build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofByteArray())
            return ObjectMapper().readTree(response.body()).get("value").map { it }

        } catch (e: Exception) {
            throw SapItemFetchException("Failed to fetch items from SAP: ${e.message}")
        }
    }

    /**
     * Gets item count from sap. Not currently needed but count will be useful in the future if SAP performance
     * is insufficient for large requests
     *
     * @param resourceUrl resource URL
     * @param sapSession current active SAP session
     * @param filter filter string or null
     * @return count of items
     */
    @Suppress("unused")
    fun getCountFromSap(resourceUrl: String, sapSession: SapSession, filter: String? = null): Int {
        var countUrl = "$resourceUrl/\$count?"

        if (filter != null) {
            val escapedFilter = escapeSapQuery(filter)
            countUrl = "$countUrl$escapedFilter"
        }

        return getCountRequest(countUrl = countUrl, sessionId = sapSession.sessionId, routeId = sapSession.routeId)
    }

    /**
     * Sends a POST or PATCH request to SAP
     *
     * @param item body to send
     * @param resourceUrl resource url
     * @param sessionId SAP session id
     * @param routeId SAP session route id
     * @param method request method
     * @return the response from SAP
     */
    private fun sendSapPostOrPatchRequest(
        item: JsonNode,
        resourceUrl: String,
        sessionId: String,
        routeId: String,
        method: String
    ): JsonNode {
        val client = HttpClient.newHttpClient()
        val request = HttpRequest
            .newBuilder(URI.create(resourceUrl))
            .setHeader("Cookie", "B1SESSION=$sessionId; ROUTEID=$routeId")
            .method(method, HttpRequest.BodyPublishers.ofString(item.toString()))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofByteArray())
        return ObjectMapper().readTree(response.body()).get("value")
    }

    /**
     * Sends get count request to SAP
     *
     * @param countUrl an url to get an item count
     * @param sessionId SAP-session id
     * @param routeId SAP-session route id
     * @return item count
     */
    private fun getCountRequest(countUrl: String, sessionId: String, routeId: String): Int {
        try {
            val client = HttpClient.newHttpClient()
            val request = HttpRequest
                .newBuilder(URI.create(countUrl))
                .setHeader("Cookie", "B1SESSION=$sessionId; ROUTEID=$routeId")
                .GET()
                .build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            return response.body().toInt()
        } catch (e: Exception) {
            throw SapCountFetchException("Failed to fetch item count from SAP, ${e.message}")
        }
    }

    /**
     * Escapes characters in a SAP-query that would otherwise cause errors
     *
     * @param query a query to escape
     * @return escaped query
     */
    private fun escapeSapQuery (query: String): String {
        return query.replace(" ", "%20").replace("'", "%27")
    }

}