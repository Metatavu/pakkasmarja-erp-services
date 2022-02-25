package fi.metatavu.pakkasmarja.services.erp.test.functional.tests

import fi.metatavu.pakkasmarja.services.erp.test.client.models.SapContract
import fi.metatavu.pakkasmarja.services.erp.test.client.models.SapContractStatus
import fi.metatavu.pakkasmarja.services.erp.test.functional.resources.LocalTestProfile
import fi.metatavu.pakkasmarja.services.erp.test.functional.resources.SapMockTestResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * Tests for contracts
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(SapMockTestResource::class)
)
@TestProfile(LocalTestProfile::class)
class ContractsResourceTest: AbstractResourceTest() {

    /**
     * Test listing contracts
     */
    @Test
    fun testListContracts() {
        createTestBuilder().use {
            val contracts = it.manager.contracts.list(sapContractStatus = null)
            assertEquals(5, contracts.size)
            val contractToTest = contracts.find { contract -> contract.status == SapContractStatus.APPROVED }
            assertNotNull(contractToTest)
            val onHolds = contracts.filter { contract -> contract.status == SapContractStatus.ON_HOLD }
            assertNotNull(onHolds)
            assertEquals(2, onHolds.size)
            assertNotNull(contracts.find { contract -> contract.status == SapContractStatus.DRAFT })
            assertNotNull(contracts.find { contract -> contract.status == SapContractStatus.TERMINATED })
            assertNotNull(contracts.find { contract -> contract.itemGroupCode == 101 })

            assertEquals(23, contractToTest!!.businessPartnerCode)
            assertEquals(64, contractToTest.contactPersonCode)
            assertEquals("2022-01-01", contractToTest.startDate)
            assertEquals("2022-01-01", contractToTest.signingDate)
            assertEquals("2022-12-31", contractToTest.endDate)
            assertEquals("2022-12-31", contractToTest.terminateDate)
            assertEquals("Some remarks", contractToTest.remarks)
            assertEquals("2022-1", contractToTest.id)
            assertEquals(2.0, contractToTest.deliveredQuantity)
            assertEquals(100, contractToTest.itemGroupCode)
            val filteredContracts = it.manager.contracts.list(sapContractStatus = SapContractStatus.APPROVED)
            assertEquals(1, filteredContracts.size)
            assertEquals(SapContractStatus.APPROVED, filteredContracts[0].status)
        }
    }

    /**
     * Test listing contracts when the access token is null
     */
    @Test
    fun testListContractsNullAccessToken() {
        createTestBuilder().use {
            it.nullAccess.contracts.assertListFailStatus(401)
        }
    }

    /**
     * Test listing contracts when the access token is invalid
     */
    @Test
    fun testListContractsInvalidAccessToken() {
        createTestBuilder().use {
            it.invalidAccess.contracts.assertListFailStatus(401)
        }
    }

    /**
     * Test creating a contract when the access token is null
     */
    @Test
    fun testCreateContractNullAccessToken() {
        createTestBuilder().use {
            it.nullAccess.contracts.assertCreateFailStatus(401)
        }
    }

    /**
     * Test creating a contract when the access token is invalid
     */
    @Test
    fun testCreateContractInvalidAccessToken() {
        createTestBuilder().use {
            it.invalidAccess.contracts.assertCreateFailStatus(401)
        }
    }

    /**
     * Tests creating a contract
     */
    @Test
    fun testCreateContract() {
        createTestBuilder().use {
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

            val createdContract = it.manager.contracts.create(newContract)
            assertEquals(newContract, createdContract)
        }
    }

    /**
     * Tests updating a contract
     */
    @Test
    fun testUpdateContract() {
        createTestBuilder().use {
            val newContract = SapContract(
                id = "2022-1",
                businessPartnerCode = 123,
                contactPersonCode = 122,
                itemGroupCode = 101,
                status = SapContractStatus.APPROVED,
                deliveredQuantity = 1.0,
                startDate = "2022-01-01",
                endDate = "2022-12-31",
                terminateDate = "2022-12-31",
                signingDate = "2022-01-01",
                remarks = "Remarks"
            )

            val createdContract = it.manager.contracts.create(newContract)
            assertEquals(newContract, createdContract)
        }
    }
}