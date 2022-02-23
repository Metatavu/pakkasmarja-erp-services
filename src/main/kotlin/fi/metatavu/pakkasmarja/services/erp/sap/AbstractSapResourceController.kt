package fi.metatavu.pakkasmarja.services.erp.sap

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.pakkasmarja.services.erp.sap.exception.SapCountFetchException
import fi.metatavu.pakkasmarja.services.erp.sap.exception.SapItemFetchException
import fi.metatavu.pakkasmarja.services.erp.sap.exception.SapModificationException
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
    fun createItem(item: JsonNode, resourceUrl: String, sessionId: String, routeId: String): JsonNode {
        try {
            return sendSapRequestWithItem(item = item, resourceUrl = resourceUrl, sessionId = sessionId, routeId = routeId, method = "POST")
        } catch (e: Exception) {
            throw SapModificationException("Failed to create an item to SAP: ${e.message}")
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
    fun updateItem(item: JsonNode, resourceUrl: String, sessionId: String, routeId: String): JsonNode {
        try {
            return sendSapRequestWithItem(item = item, resourceUrl = resourceUrl, sessionId = sessionId, routeId = routeId, method = "PATCH")
        } catch (e: Exception) {
            throw SapModificationException("Failed to update an item to SAP: ${e.message}")
        }
    }

    private fun sendSapRequestWithItem(item: JsonNode, resourceUrl: String, sessionId: String, routeId: String, method: String): JsonNode {
        val client = HttpClient.newHttpClient()
        val request = HttpRequest
            .newBuilder(URI.create(resourceUrl))
            .setHeader("Cookie", "B1SESSION=$sessionId; ROUTEID=$routeId")
            .setHeader("Prefer","odata.maxpagesize=100")
            .method(method, HttpRequest.BodyPublishers.ofByteArray(item.binaryValue()))
            .build()

        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray()).thenApply { response ->
            val objectMapper = ObjectMapper()
            return@thenApply objectMapper.readTree(response.body()).get("value")
        }

        return response.get()
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
     * Gets items from SAP and converts them to JSON-nodes
     *
     * @param resourceUrl resource url
     * @param filter SAP-filter
     * @param select SAP-field selector
     * @param routeId SAP-session route id
     * @param sessionId SAP-session id
     * @return items
     */
    fun getItemsAsJsonNodes(
        resourceUrl: String,
        filter: String,
        select: String,
        routeId: String,
        sessionId: String
    ): List<JsonNode> {
        val escapedFilter = escapeSapQuery(filter)
        val countUrl = "$resourceUrl/\$count?$escapedFilter"
        val count = getCount(countUrl = countUrl, sessionId = sessionId, routeId = routeId)
        val baseItemUrl = "$resourceUrl?$escapedFilter&$select"
        val itemUrls = getItemUrls(baseItemUrl = baseItemUrl, count = count)
        return getItems(itemUrls = itemUrls, sessionId = sessionId, routeId = routeId)
    }

    /**
     * Gets items from SAP and converts them to JSON-nodes
     *
     * @param resourceUrl resource url
     * @param select SAP-field selector
     * @param routeId SAP-session route id
     * @param sessionId SAP-session id
     * @return items
     */
    fun getItemsAsJsonNodes(
        resourceUrl: String,
        select: String,
        routeId: String,
        sessionId: String
    ): List<JsonNode> {
        val countUrl = "$resourceUrl/\$count"
        val count = getCount(countUrl = countUrl, sessionId = sessionId, routeId = routeId)
        val baseItemUrl = "$resourceUrl?$select"
        val itemUrls = getItemUrls(baseItemUrl = baseItemUrl, count = count)
        return getItems(itemUrls = itemUrls, sessionId = sessionId, routeId = routeId)
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
     * Fetches items from multiple urls and combines them into a single list
     *
     * @param itemUrls item urls
     * @param sessionId SAP-session id
     * @param routeId SAP-session route id
     * @return items
     */
    private fun getItems(itemUrls: List<String>, sessionId: String, routeId: String): List<JsonNode> {
        try {
            val jsonNodes = mutableListOf<JsonNode>()
            val client = HttpClient.newHttpClient()
            val futures = mutableListOf<CompletableFuture<Unit>>()

            itemUrls.forEach {
                    val request = HttpRequest
                        .newBuilder(URI.create(it))
                        .setHeader("Cookie", "B1SESSION=$sessionId; ROUTEID=$routeId")
                        .setHeader("Prefer","odata.maxpagesize=100")
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

    /**
     * Gets an item count
     *
     * @param countUrl an url to get an item count
     * @param sessionId SAP-session id
     * @param routeId SAP-session route id
     * @return item count
     */
    private fun getCount(countUrl: String, sessionId: String, routeId: String): Int {
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
}