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
import java.util.concurrent.CompletableFuture
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
            return sendSapPostRequest(
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
            return sendSapPostRequest(
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
     * Gets data from SAP and converts them to JSON-nodes
     *
     * @param resourceUrl resource url
     * @param filter SAP-filter
     * @param select SAP-field selector
     * @param routeId SAP-session route id
     * @param sessionId SAP-session id
     * @return items
     */
    fun getDataFromSap(
        resourceUrl: String,
        filter: String,
        select: String,
        routeId: String,
        sessionId: String
    ): List<JsonNode> {
        val escapedFilter = escapeSapQuery(filter)
        val countUrl = "$resourceUrl/\$count?$escapedFilter"
        val count = getCountRequest(countUrl = countUrl, sessionId = sessionId, routeId = routeId)
        val baseItemUrl = "$resourceUrl?$escapedFilter&$select"
        val itemUrls = getItemUrls(baseItemUrl = baseItemUrl, count = count)
        return getItemsRequest(itemUrls = itemUrls, sessionId = sessionId, routeId = routeId)
    }

    /**
     * Gets data from SAP and converts them to JSON-nodes
     *
     * @param resourceUrl resource url
     * @param select SAP-field selector
     * @param routeId SAP-session route id
     * @param sessionId SAP-session id
     * @return items
     */
    fun getDataFromSap(
        resourceUrl: String,
        select: String,
        routeId: String,
        sessionId: String
    ): List<JsonNode> {
        val countUrl = "$resourceUrl/\$count"
        val count = getCountRequest(countUrl = countUrl, sessionId = sessionId, routeId = routeId)
        val baseItemUrl = "$resourceUrl?$select"
        val itemUrls = getItemUrls(baseItemUrl = baseItemUrl, count = count)
        return getItemsRequest(itemUrls = itemUrls, sessionId = sessionId, routeId = routeId)
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

    fun constructSAPUrlWithFilter(resourceUrl: String, sessionId: String, routeId: String, select: String, filter: String): List<String> {
        val escapedFilter = escapeSapQuery(filter)
        val countUrl = "$resourceUrl/\$count?$escapedFilter"
        val count = getCountRequest(countUrl = countUrl, sessionId = sessionId, routeId = routeId)
        val baseItemUrl = "$resourceUrl?$escapedFilter&$select"
        return getItemUrls(baseItemUrl = baseItemUrl, count = count)
    }

    fun constructSAPUrl(resourceUrl: String, sessionId: String, routeId: String, select: String): List<String> {
        val countUrl = "$resourceUrl/\$count"
        val count = getCountRequest(countUrl = countUrl, sessionId = sessionId, routeId = routeId)
        val baseItemUrl = "$resourceUrl?$select"
        return getItemUrls(baseItemUrl = baseItemUrl, count = count)
    }

    /**
     * Fetches items from multiple urls and combines them into a single list
     *
     * @param itemUrls item urls
     * @param sapSession SAP-session
     * @return list of items
     */
    fun getItemsRequest(itemUrls: List<String>, sapSession: SapSession, maxResults: Int? = 100): List<JsonNode> {
        try {

            val jsonNodes = mutableListOf<JsonNode>()
            val client = HttpClient.newHttpClient()
            val futures = mutableListOf<CompletableFuture<Unit>>()

            itemUrls.forEach {
                val request = HttpRequest
                    .newBuilder(URI.create(it))
                    .setHeader("Cookie", "B1SESSION=${sapSession.sessionId}; ROUTEID=${sapSession.routeId}")
                    .setHeader("Prefer","odata.maxpagesize=${maxResults}")
                    .GET()
                    .build()

                val future = client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray()).thenApply { response ->
                    val objectMapper = ObjectMapper()
                    val items = objectMapper.readTree(response.body()).get("value")
                    items.forEach { item ->
                        jsonNodes.add(item)
                    }
                }
                futures.add(future)
            }

            futures.forEach(CompletableFuture<Unit>::join)
            return jsonNodes
        } catch (e: Exception) {
            throw SapItemFetchException("Failed to fetch items from SAP: ${e.message}")
        }
    }

    /**
     * Sends a POST request to SAP
     *
     * @param item body to send
     * @param resourceUrl resource url
     * @param sessionId SAP session id
     * @param routeId SAP session route id
     * @param method request method
     * @return the response from SAP
     */
    private fun sendSapPostRequest(item: JsonNode, resourceUrl: String, sessionId: String, routeId: String, method: String): JsonNode {
        val client = HttpClient.newHttpClient()
        val request = HttpRequest
            .newBuilder(URI.create(resourceUrl))
            .setHeader("Cookie", "B1SESSION=$sessionId; ROUTEID=$routeId")
            .setHeader("Prefer","odata.maxpagesize=100")
            .method(method, HttpRequest.BodyPublishers.ofString(item.toString()))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofByteArray())

        return ObjectMapper().readTree(response.body()).get("value")
    }

    /**
     * Gets an item count
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

    /**
     * Creates multiple urls in order to split a request
     *
     * @param baseItemUrl base url for all requests
     * @param count item count
     * @return item urls
     */
    private fun getItemUrls(baseItemUrl: String, count: Int): List<String> {
        val itemUrls = mutableListOf<String>()
        for (i in 0..count step 100) {
            itemUrls.add("$baseItemUrl&\$skip=$i")
        }

        return itemUrls
    }

}