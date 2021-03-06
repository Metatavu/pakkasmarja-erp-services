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
     * Creates a new SAP contract
     *
     * @param sapContract a SAP contract to create
     * @return created SAP contract
     */
    fun create(sapContract: SapContract): SapContract {
        return api.createContract(sapContract)
    }

    override fun getApi(): ContractsApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return ContractsApi(ApiTestSettings.apiBasePath)
    }

    /**
     * Asserts that listing fails with expected status
     * @param expectedStatus expected status
     */
    fun assertListFailStatus(expectedStatus: Int) {
        try {
            list(sapContractStatus = null)
            Assertions.fail(String.format("Expected listing to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assertions.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts that creation fails with expected status
     * @param expectedStatus expected status
     */
    fun assertCreateFailStatus(expectedStatus: Int) {
        try {
            val newContract = SapContract(
                id = "2022-1",
                businessPartnerCode = 122,
                contactPersonCode = 122,
                itemGroupCode = 100,
                status = SapContractStatus.APPROVED,
                deliveredQuantity = 2.0,
                startDate = "2022-01-01",
                endDate = "2022-12-31",
                terminateDate = "2022-12-31",
                signingDate = "2022-01-01",
                remarks = "Remarks"
            )

            create(newContract)
            Assertions.fail(String.format("Expected listing to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assertions.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }
}