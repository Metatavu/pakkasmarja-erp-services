package fi.metatavu.pakkasmarja.services.erp.sap

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.pakkasmarja.services.erp.sap.exception.SapCountFetchException
import fi.metatavu.pakkasmarja.services.erp.sap.exception.SapItemFetchException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.OffsetDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionService
import javax.enterprise.context.ApplicationScoped

/**
 * Abstract SAP-resource controller
 */
@ApplicationScoped
abstract class AbstractSapResourceController {

    /**
     * Gets items from SAP and converts them to JSON-nodes
     *
     * @param resourceUrl resource url
     * @param filter SAP-filter
     * @param select SAP-field selector
     * @param routeId SAP-session route id
     * @param sessionId SAP-session id
     *
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
     * Escapes characters in a SAP-query that would otherwise cause errors
     *
     * @param query a query to escape
     *
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
     *
     * @return items
     */
    private fun getItems(itemUrls: List<String>, sessionId: String, routeId: String): List<JsonNode> {
        val jsonNodes = mutableListOf<JsonNode>()
        val client = HttpClient.newHttpClient()
        val futures = mutableListOf<CompletableFuture<Unit>>()

        itemUrls.forEach {
            try {
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
            } catch (e: Exception) {
                throw SapItemFetchException("Failed to fetch items from SAP: ${e.message}")
            }
        }

        futures.forEach(CompletableFuture<Unit>::join)
        return jsonNodes
    }

    /**
     * Creates multiple urls in order to split a request
     *
     * @param baseItemUrl base url for all requests
     * @param count item count
     *
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
     *
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

    /**
     * Creates an "updatedAfter"-filter from an OffsetDateTime-object
     *
     * @param updatedAfter a value to be used for the filter
     *
     * @return "updatedAfter"-filter
     */
    fun createdUpdatedAfterFilter (updatedAfter: OffsetDateTime): String {
        val updateDate = updatedAfter.toLocalDate().toString()
        val updateTime = updatedAfter.toLocalTime().toString().split(".")[0]
        return "(UpdateDate gt '$updateDate' or (UpdateDate eq '$updateDate' and UpdateTime gt '$updateTime'))"
    }
}