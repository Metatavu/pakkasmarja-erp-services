package fi.metatavu.pakkasmarja.services.erp.test.functional.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.pakkasmarja.services.erp.test.client.apis.BusinessPartnersApi
import fi.metatavu.pakkasmarja.services.erp.test.client.infrastructure.ApiClient
import fi.metatavu.pakkasmarja.services.erp.test.client.infrastructure.ClientException
import fi.metatavu.pakkasmarja.services.erp.test.client.models.SapBusinessPartner
import fi.metatavu.pakkasmarja.services.erp.test.functional.TestBuilder
import fi.metatavu.pakkasmarja.services.erp.test.functional.settings.ApiTestSettings

import org.junit.jupiter.api.Assertions.*

/**
 * Resource for testing business partners API
 *
 * @author Jari Nykänen
 */
class BusinessPartnersTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
):ApiTestBuilderResource<SapBusinessPartner, ApiClient?>(testBuilder, apiClient) {

    override fun clean(example: SapBusinessPartner) {

    }

    override fun getApi(): BusinessPartnersApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return BusinessPartnersApi(ApiTestSettings.apiBasePath)
    }

    /**
     * Lists business partners
     *
     * @param updatedAfter Filter results by updated after given date time (optional)
     * @param firstResult first result
     * @param maxResults max results
     * @return list of business partners
     */
    fun listBusinessPartners(
        updatedAfter: String?,
        firstResult: Int?,
        maxResults: Int?
    ): Array<SapBusinessPartner> {
        return api.listBusinessPartners(
            updatedAfter = updatedAfter,
            firstResult = firstResult,
            maxResults = maxResults
        )
    }


    /**
     * Asserts that listing business partners fails with the status
     *
     * @param expectedStatus expected status
     * @param updatedAfter Filter results by updated after given date time (optional)
     * @param firstResult first result
     * @param maxResults max results
     */
    fun assertListFailStatus(
        expectedStatus: Int,
        updatedAfter: String?,
        firstResult: Int?,
        maxResults: Int?
    ) {
        try {
            listBusinessPartners(
                updatedAfter = updatedAfter,
                firstResult = firstResult,
                maxResults = maxResults
            )

            fail(String.format("Expected listing to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

}
