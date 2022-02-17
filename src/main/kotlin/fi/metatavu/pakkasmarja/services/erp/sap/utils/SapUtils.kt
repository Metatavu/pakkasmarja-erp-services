package fi.metatavu.pakkasmarja.services.erp.sap.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.OffsetDateTime
import javax.enterprise.context.ApplicationScoped

/**
 * Common functions for SAP-controllers
 */
@ApplicationScoped
class SapUtils {
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
    ): ArrayList<JsonNode> {
        val countUrl = "$resourceUrl/\$count?$filter"
        val count = getCount(countUrl = countUrl, sessionId = sessionId, routeId = routeId)
        val baseItemUrl = "$resourceUrl?$filter&$select"
        val itemUrls = getItemUrls(baseItemUrl = baseItemUrl, count = count)
        return getItems(itemUrls = itemUrls, sessionId = sessionId, routeId = routeId)
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
    private fun getItems(itemUrls: List<String>, sessionId: String, routeId: String): ArrayList<JsonNode> {
        val jsonNodes = ArrayList<JsonNode>()
        itemUrls.forEach {
            try {
                val request = HttpRequest
                    .newBuilder(URI.create(it))
                    .setHeader("Cookie", "B1SESSION=$sessionId; ROUTEID=$routeId")
                    .setHeader("Prefer","odata.maxpagesize=100")
                    .GET()
                    .build()

                val client = HttpClient.newHttpClient()
                client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray()).thenApply { response ->
                    val objectMapper = ObjectMapper()
                    val items = objectMapper.readTree(response.body())
                    items.forEach { item ->
                        jsonNodes.add(item)
                    }
                }
            } catch (e: Exception) {
                throw SapItemFetchException("Failed to fetch items from SAP: ${e.message}")
            }
        }

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
    private fun getItemUrls(baseItemUrl: String, count: Int): ArrayList<String> {
        val itemUrls = ArrayList<String>()
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
                .POST(HttpRequest.BodyPublishers.noBody())
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
        return "(UpdateDate gt $updateDate or (UpdateDate eq $updateDate and UpdateTime gt $updateTime))"
    }
}