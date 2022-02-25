package fi.metatavu.pakkasmarja.services.erp.test.functional.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.pakkasmarja.services.erp.test.client.apis.ContractsApi
import fi.metatavu.pakkasmarja.services.erp.test.client.infrastructure.ApiClient
import fi.metatavu.pakkasmarja.services.erp.test.client.infrastructure.ClientException
import fi.metatavu.pakkasmarja.services.erp.test.client.models.SapContract
import fi.metatavu.pakkasmarja.services.erp.test.client.models.SapContractStatus
import fi.metatavu.pakkasmarja.services.erp.test.functional.TestBuilder
import fi.metatavu.pakkasmarja.services.erp.test.functional.settings.ApiTestSettings
import org.junit.jupiter.api.Assertions

/**
 * Resource for testing Contracts API
 */
class ContractsResourceTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
): ApiTestBuilderResource<SapContract, ApiClient?>(testBuilder, apiClient) {
    override fun clean(t: SapContract?) {}

    /**
     * Lists all contracts in SAP, from beginning of the year 2022
     *
     * @param sapContractStatus SAP contract status to filter by
     * @return SAP contracts
     */
    fun list(sapContractStatus: SapContractStatus?): Array<SapContract> {
        return api.listContracts(startDate = null, businessPartnerCode = null, contractStatus = sapContractStatus)
    }

    /**
     * Creates a contract to SAP
     *
     * @param sapContract a contract to create
     * @return created contract
     */
    fun create(sapContract: SapContract): SapContract {
        return api.createContract(sapContract)
    }

    override fun getApi(): ContractsApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return ContractsApi(ApiTestSettings.apiBasePath)
    }

    fun assertListFailStatus(expectedStatus: Int) {
        try {
            list(sapContractStatus = null)
            Assertions.fail(String.format("Expected listing to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assertions.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }
}