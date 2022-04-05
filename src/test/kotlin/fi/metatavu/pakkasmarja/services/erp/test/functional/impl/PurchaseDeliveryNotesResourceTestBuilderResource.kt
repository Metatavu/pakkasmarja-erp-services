package fi.metatavu.pakkasmarja.services.erp.test.functional.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.pakkasmarja.services.erp.test.client.apis.PurchaseDeliveryNotesApi
import fi.metatavu.pakkasmarja.services.erp.test.client.infrastructure.ApiClient
import fi.metatavu.pakkasmarja.services.erp.test.client.models.SapPurchaseDeliveryNote
import fi.metatavu.pakkasmarja.services.erp.test.functional.TestBuilder
import fi.metatavu.pakkasmarja.services.erp.test.functional.settings.ApiTestSettings

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

    override fun getApi(): PurchaseDeliveryNotesApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return PurchaseDeliveryNotesApi(ApiTestSettings.apiBasePath)
    }

}