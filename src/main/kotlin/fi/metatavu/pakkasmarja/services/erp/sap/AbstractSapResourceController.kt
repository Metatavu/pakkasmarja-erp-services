package fi.metatavu.pakkasmarja.services.erp.sap

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.metatavu.pakkasmarja.services.erp.sap.exception.SapCountFetchException
import fi.metatavu.pakkasmarja.services.erp.sap.exception.SapItemFetchException
import fi.metatavu.pakkasmarja.services.erp.sap.exception.SapModificationException
import fi.metatavu.pakkasmarja.services.erp.sap.session.SapSession
import org.slf4j.Logger
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject


/**
 * Abstract SAP-resource controller
 */
abstract class AbstractSapResourceController <T> {

    @Inject
    lateinit var logger: Logger

    /**
     * Creates an item to SAP
     *
     * @param item item to create
     * @param resourceUrl resource url
     * @param sessionId SAP session id
     * @param routeId SAP session route id
     * @return created item
     */
    fun createSapEntity(
        targetClass: Class<T>,
        item: String,
        resourceUrl: String,
        sessionId: String,
        routeId: String
    ): T {
        try {
            return sendSapPostOrPatchRequest(
                targetClass = targetClass,
                item = item,
                resourceUrl = resourceUrl,
                sessionId = sessionId,
                routeId = routeId,
                method = "POST"
            )
        } catch (e: Exception) {
            logger.error("Failed to create an item to SAP", e)
            throw SapModificationException("Failed to create an item to SAP: ${e.message}")
        }
    }

    /**
     * Finds item from SAP
     *
     * @param targetClass target class
     * @param itemUrl item url
     * @param sessionId SAP-session id
     * @param routeId SAP-session route id
     * @return found item or null
     */
    fun findSapEntity(
        targetClass: Class<T>,
        itemUrl: String,
        sessionId: String,
        routeId: String
    ): T? {
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

            return readSapResponse(targetClass, response.body())
        } catch (e: Exception) {
            throw SapItemFetchException("Failed to fetch items from SAP: ${e.message}")
        }
    }

    /**
     * Updates an item to SAP
     *
     * @param targetClass target class
     * @param item item to update
     * @param resourceUrl resource url
     * @param sessionId SAP session id
     * @param routeId SAP session route id
     * @return updated item
     */
    fun updateSapEntity(
        targetClass: Class<T>,
        item: String,
        resourceUrl: String,
        sessionId: String,
        routeId: String
    ): T {
        try {
            return sendSapPostOrPatchRequest(
                targetClass = targetClass,
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
        val updateTime = updatedAfter.format(DateTimeFormatter.ISO_LOCAL_TIME)
        return "(UpdateDate gt $updateDate or (UpdateDate eq $updateDate and UpdateTime gt $updateTime))"
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
        firstResult: Int?,
        maxResults: Int?
    ): String {
        val list = mutableListOf<String>()

        if (filter != null) {
            list.add(escapeSapQuery(filter))
        }

        if (firstResult != null) {
            list.add("\$skip=$firstResult")
        }

        if (maxResults != null) {
            list.add("\$top=$maxResults")
        }

        return if (list.size > 0) {
            "$baseUrl?$select&${list.joinToString("&")}"
        } else {
            "$baseUrl?$select"
        }
    }

    /**
     * Fetches items from multiple urls and combines them into a single list
     *
     * @param targetClass target class
     * @param requestUrl list SAP request URL's
     * @param sapSession SAP-session
     * @param <T> response generic type
     * @return list of items
     */
    fun sapListRequest(targetClass: Class<T>, requestUrl: String, sapSession: SapSession): List<T>? {
        try {
            val client = HttpClient.newHttpClient()

            val request = HttpRequest
                .newBuilder(URI.create(requestUrl))
                .setHeader("Cookie", "B1SESSION=${sapSession.sessionId}; ROUTEID=${sapSession.routeId}")
                .GET()
                .build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofByteArray())

            if (response.statusCode() != 200) {
                return null
            }

            return readSapListResponse(targetClass, response.body())
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
    fun getCountFromSap(resourceUrl: String, sapSession: SapSession, filter: String? = null): Int? {
        var countUrl = "$resourceUrl/\$count?"

        if (filter != null) {
            val escapedFilter = escapeSapQuery(filter)
            countUrl = "$countUrl$escapedFilter"
        }

        return getCountRequest(countUrl = countUrl, sessionId = sapSession.sessionId, routeId = sapSession.routeId)
    }

    /**
     * Translates a boolean value to the format used by SAP
     *
     * @param value a value to translate
     * @return a boolean value in the format used by SAP
     */
    fun toSapItemPropertyBoolean(value: Boolean): String {
        return when (value) {
            true -> "tYES"
            false -> "tNO"
        }
    }

    /**
     * Sends a POST or PATCH request to SAP
     *
     * @param targetClass target class
     * @param item body to send
     * @param resourceUrl resource url
     * @param sessionId SAP session id
     * @param routeId SAP session route id
     * @param method request method
     * @param <T> response type
     * @return the response from SAP
     */
    private fun <T> sendSapPostOrPatchRequest(
        targetClass: Class<T>,
        item: String,
        resourceUrl: String,
        sessionId: String,
        routeId: String,
        method: String
    ): T {
        val client = HttpClient.newHttpClient()
        val request = HttpRequest
            .newBuilder(URI.create(resourceUrl))
            .setHeader("Content-Type", "application/json")
            .setHeader("Cookie", "B1SESSION=$sessionId; ROUTEID=$routeId")
            .method(method, HttpRequest.BodyPublishers.ofString(item))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofByteArray())
        val body = response.body() ?: throw SapModificationException("Failed to fetch items from SAP: ${response.statusCode()}")

        if (response.statusCode() != 200) {
            throw SapModificationException("Failed send $method request to $resourceUrl: ${body.toString(Charsets.UTF_8)}")
        }

        if (body.isEmpty()) {
            throw SapModificationException("Failed send $method request to $resourceUrl: Empty response")
        }

        return readSapResponse(targetClass, body) ?: throw SapModificationException("Failed to read response from SAP: ${body.toString(Charsets.UTF_8)}")
    }

    /**
     * Sends get count request to SAP
     *
     * @param countUrl an url to get an item count
     * @param sessionId SAP-session id
     * @param routeId SAP-session route id
     * @return item count
     */
    private fun getCountRequest(countUrl: String, sessionId: String, routeId: String): Int? {
        try {
            val client = HttpClient.newHttpClient()
            val request = HttpRequest
                .newBuilder(URI.create(countUrl))
                .setHeader("Cookie", "B1SESSION=$sessionId; ROUTEID=$routeId")
                .GET()
                .build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() != 200) {
                return null
            }

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
     * Reads SAP resnpose from raw response
     *
     * @param body response body
     * @param targetClass target class
     * @return SAP response
     */
    private fun <T> readSapResponse(targetClass: Class<T>, body: ByteArray): T {
        val objectMapper = jacksonObjectMapper()
        val type = objectMapper.typeFactory.constructType(targetClass)
        return objectMapper.convertValue(ObjectMapper().readTree(body), type)
    }

    /**
     * Reads SAP list response from raw response
     *
     * @param body response body
     * @param targetClass target class
     * @return SAP list response
     */
    private fun <T> readSapListResponse(targetClass: Class<T>, body: ByteArray): List<T> {
        val objectMapper = jacksonObjectMapper()
        val responseValue = objectMapper.readTree(body).get("value").map { it }
        val collectionType = objectMapper.typeFactory.constructCollectionType(ArrayList::class.java, targetClass)
        return objectMapper.convertValue(responseValue, collectionType)
    }
}