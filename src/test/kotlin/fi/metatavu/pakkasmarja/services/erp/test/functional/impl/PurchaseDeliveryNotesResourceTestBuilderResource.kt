package fi.metatavu.pakkasmarja.services.erp.test.functional.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.pakkasmarja.services.erp.test.client.apis.PurchaseDeliveryNotesApi
import fi.metatavu.pakkasmarja.services.erp.test.client.infrastructure.ApiClient
import fi.metatavu.pakkasmarja.services.erp.test.client.infrastructure.ClientException
import fi.metatavu.pakkasmarja.services.erp.test.client.models.SapPurchaseDeliveryNote
import fi.metatavu.pakkasmarja.services.erp.test.functional.TestBuilder
import fi.metatavu.pakkasmarja.services.erp.test.functional.settings.ApiTestSettings
import org.junit.jupiter.api.Assertions

/**
 * Resource for testing purchase delivery notes API
 */
class PurchaseDeliveryNotesResourceTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
): ApiTestBuilderResource<SapPurchaseDeliveryNote, ApiClient?>(testBuilder, apiClient) {
    override fun clean(t: SapPurchaseDeliveryNote?) {}

    /**
     * Creates a new SAP purchase delivery note
     *
     * @param sapPurchaseDeliveryNote a SAP purchase delivery note to create
     * @return created SAP purchase delivery note
     */
    fun create(sapPurchaseDeliveryNote: SapPurchaseDeliveryNote): SapPurchaseDeliveryNote {
        return api.createPurchaseDeliveryNote(sapPurchaseDeliveryNote)
    }

    /**
     * Asserts that creating a new SAP purchase delivery note fails
     *
     * @param expectedStatus expected fail status
     * @param sapPurchaseDeliveryNote a SAP purchase delivery note to create
     */
    fun assertCreateFail(expectedStatus: Int, sapPurchaseDeliveryNote: SapPurchaseDeliveryNote) {
        try {
            api.createPurchaseDeliveryNote(sapPurchaseDeliveryNote)
            Assertions.fail(String.format("Expected creating to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assertions.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    override fun getApi(): PurchaseDeliveryNotesApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return PurchaseDeliveryNotesApi(ApiTestSettings.apiBasePath)
    }

}