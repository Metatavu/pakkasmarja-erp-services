package fi.metatavu.pakkasmarja.services.erp.test.functional.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.pakkasmarja.services.erp.test.client.apis.StockTransfersApi
import fi.metatavu.pakkasmarja.services.erp.test.client.infrastructure.ApiClient
import fi.metatavu.pakkasmarja.services.erp.test.client.models.SapStockTransfer
import fi.metatavu.pakkasmarja.services.erp.test.functional.TestBuilder
import fi.metatavu.pakkasmarja.services.erp.test.functional.settings.ApiTestSettings

/**
 * Resource for testing Stock transfers API
 */
class StockTransfersResourceTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
): ApiTestBuilderResource<SapStockTransfer, ApiClient?>(testBuilder, apiClient) {
    override fun clean(t: SapStockTransfer?) {}

    /**
     * Creates a new SAP stock transfer
     *
     * @param sapStockTransfer a SAP stock transfer to create
     * @return created SAP stock transfer
     */
    fun create(sapStockTransfer: SapStockTransfer): SapStockTransfer {
        return api.createStockTransfer(sapStockTransfer)
    }

    override fun getApi(): StockTransfersApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return StockTransfersApi(ApiTestSettings.apiBasePath)
    }

}