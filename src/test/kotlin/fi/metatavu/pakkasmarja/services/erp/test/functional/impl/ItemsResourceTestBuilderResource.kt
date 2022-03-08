package fi.metatavu.pakkasmarja.services.erp.test.functional.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.pakkasmarja.services.erp.test.client.apis.ItemsApi
import fi.metatavu.pakkasmarja.services.erp.test.client.infrastructure.ApiClient
import fi.metatavu.pakkasmarja.services.erp.test.client.infrastructure.ClientException
import fi.metatavu.pakkasmarja.services.erp.test.client.models.SapContract
import fi.metatavu.pakkasmarja.services.erp.test.client.models.SapItem
import fi.metatavu.pakkasmarja.services.erp.test.functional.TestBuilder
import fi.metatavu.pakkasmarja.services.erp.test.functional.settings.ApiTestSettings
import org.junit.jupiter.api.Assertions

/**
 * Resource for testing Items API
 */
class ItemsResourceTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
): ApiTestBuilderResource<SapContract, ApiClient?>(testBuilder, apiClient) {
    override fun clean(t: SapContract?) {}

    override fun getApi(): ItemsApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return ItemsApi(ApiTestSettings.apiBasePath)
    }

    /**
     * Lists all items in SAP
     *
     * @param itemGroupCode filter by item group code
     * @param updatedAfter filter by update after date
     * @param firstResult first result
     * @param maxResults max amount of results
     * @return list of items
     */
    fun list(
        itemGroupCode: Int?,
        updatedAfter: String?,
        firstResult: Int?,
        maxResults: Int?
    ): Array<SapItem> {
        return api.listItems(
            itemGroupCode = itemGroupCode,
            updatedAfter = updatedAfter,
            firstResult = firstResult,
            maxResults = maxResults
        )
    }

    /**
     * Finds SAP item with given SAP ID
     *
     * @param sapId SAP ID to search
     * @return found SAP item
     */
    fun find(sapId: Int): SapItem {
        return api.findItem(sapId = sapId)
    }

    /**
     * Asserts that listing fails with expected status
     *
     * @param expectedStatus expected status
     * @param itemGroupCode filter by item group code
     * @param updatedAfter filter by update after date
     * @param firstResult first result
     * @param maxResults max amount of results
     */
    fun assertListFailStatus(
        expectedStatus: Int,
        itemGroupCode: Int?,
        updatedAfter: String?,
        firstResult: Int?,
        maxResults: Int?
    ) {
        try {
            list(
                itemGroupCode = itemGroupCode,
                updatedAfter = updatedAfter,
                firstResult = firstResult,
                maxResults = maxResults
            )
            Assertions.fail(String.format("Expected listing to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assertions.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts that finding fails with expected status
     *
     * @param expectedStatus expected status
     * @param sapId SAP ID
     */
    fun assertFindFailStatus(expectedStatus: Int, sapId: Int) {
        try {
            api.findItem(sapId = sapId)
            Assertions.fail(String.format("Expected listing to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assertions.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

}