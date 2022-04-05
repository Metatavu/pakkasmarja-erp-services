package fi.metatavu.pakkasmarja.services.erp.test.functional.impl.auth

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenTestBuilderAuthentication
import fi.metatavu.pakkasmarja.services.erp.test.client.infrastructure.ApiClient
import fi.metatavu.pakkasmarja.services.erp.test.functional.TestBuilder
import fi.metatavu.pakkasmarja.services.erp.test.functional.impl.*
import fi.metatavu.pakkasmarja.services.erp.test.functional.settings.ApiTestSettings


/**
 * Test builder authentication
 *
 * @author Jari Nykänen
 * @author Antti Leppä
 *
 * @param testBuilder test builder instance
 * @param accessTokenProvider access token provider
 */
class TestBuilderAuthentication(
    private val testBuilder: TestBuilder,
    accessTokenProvider: AccessTokenProvider
): AccessTokenTestBuilderAuthentication<ApiClient>(testBuilder, accessTokenProvider) {

    private var accessTokenProvider: AccessTokenProvider? = accessTokenProvider

    val businessPartners = BusinessPartnersTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val contracts = ContractsResourceTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val items = ItemsResourceTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val stockTransfers = StockTransfersResourceTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())
    val purchaseDeliveryNotes = PurchaseDeliveryNotesResourceTestBuilderResource(testBuilder, this.accessTokenProvider, createClient())

    override fun createClient(authProvider: AccessTokenProvider): ApiClient {
        val result = ApiClient(ApiTestSettings.apiBasePath)
        ApiClient.accessToken = authProvider.accessToken
        return result
    }

}