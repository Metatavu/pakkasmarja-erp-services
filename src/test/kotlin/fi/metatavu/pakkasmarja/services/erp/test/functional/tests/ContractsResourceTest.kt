package fi.metatavu.pakkasmarja.services.erp.test.functional.tests

import fi.metatavu.pakkasmarja.services.erp.test.client.models.SapContract
import fi.metatavu.pakkasmarja.services.erp.test.client.models.SapContractStatus
import fi.metatavu.pakkasmarja.services.erp.test.functional.resources.LocalTestProfile
import fi.metatavu.pakkasmarja.services.erp.test.functional.resources.SapMockTestResource
import fi.metatavu.pakkasmarja.services.erp.test.functional.sap.SapMock
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * Tests for contracts
 *
 * TODO: Add support for invalid data testing
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
            SapMock().use { sapMock ->
                sapMock.mockItems("1", "2", "3")
                sapMock.mockContracts("1", "2", "3", "4", "5")
                val contracts = it.manager.contracts.list(sapContractStatus = null)
                assertEquals(8, contracts.size)
                val contractToTest = contracts.find { contract -> contract.businessPartnerCode == 23 }
                assertNotNull(contractToTest)
                val onHolds = contracts.filter { contract -> contract.status == SapContractStatus.ON_HOLD }
                assertNotNull(onHolds)
                assertEquals(2, onHolds.size)
                assertNotNull(contracts.find { contract -> contract.status == SapContractStatus.DRAFT })
                assertNotNull(contracts.find { contract -> contract.status == SapContractStatus.TERMINATED })
                assertNotNull(contracts.find { contract -> contract.itemGroupCode == 101 })

                assertEquals(SapContractStatus.APPROVED, contractToTest!!.status)
                assertEquals(64, contractToTest.contactPersonCode)
                assertEquals("2022-01-01", contractToTest.startDate)
                assertEquals("2022-01-01", contractToTest.signingDate)
                assertEquals("2022-12-31", contractToTest.endDate)
                assertEquals("2022-12-31", contractToTest.terminateDate)
                assertEquals("Some remarks", contractToTest.remarks)
                assertEquals("2022-1-100", contractToTest.id)
                assertEquals(0.0, contractToTest.deliveredQuantity)
                assertEquals(100, contractToTest.itemGroupCode)

                it.nullAccess.contracts.assertListFailStatus(401)
                it.invalidAccess.contracts.assertListFailStatus(401)
            }

        }
    }

    @Test
    fun testListContractsByStatus() {
        createTestBuilder().use {
            SapMock().use { sapMock ->
                sapMock.mockItems("1", "2", "3")
                sapMock.mockContracts("1", "2", "3", "4", "5")

                val filteredContracts = it.manager.contracts.list(sapContractStatus = SapContractStatus.APPROVED)
                assertEquals(4, filteredContracts.size)
                assertEquals(SapContractStatus.APPROVED, filteredContracts[0].status)
            }
        }
    }

    /**
     * Tests creating a contract
     */
    @Test
    fun testCreateContract() {
        createTestBuilder().use {
            SapMock().use { sapMock ->
                sapMock.mockItems("1", "2", "3")
                val newContract = SapContract(
                    id = "2022-1-100",
                    businessPartnerCode = 122,
                    contactPersonCode = 122,
                    itemGroupCode = 100,
                    status = SapContractStatus.APPROVED,
                    deliveredQuantity = 0.0,
                    startDate = "2022-01-01",
                    endDate = "2022-12-31",
                    terminateDate = "2022-12-31",
                    signingDate = "2022-01-01",
                    remarks = "Remarks"
                )

                val createdContract = it.manager.contracts.create(newContract)
                assertEquals(newContract, createdContract)
                it.nullAccess.contracts.assertCreateFailStatus(401)
                it.invalidAccess.contracts.assertCreateFailStatus(401)
            }
        }
    }

    /**
     * Tests updating a contract
     */
    @Test
    fun testUpdateContract() {
        createTestBuilder().use {
            SapMock().use { sapMock ->
                sapMock.mockItems("1", "2", "3")
                sapMock.mockContracts("6")
                val newContract = SapContract(
                    id = "2022-6-100",
                    businessPartnerCode = 23,
                    contactPersonCode = 64,
                    itemGroupCode = 100,
                    status = SapContractStatus.APPROVED,
                    deliveredQuantity = 0.0,
                    startDate = "2022-01-01",
                    endDate = "2022-12-31",
                    terminateDate = "2022-12-31",
                    signingDate = "2022-01-01",
                    remarks = "Some remarks"
                )

                val createdContract = it.manager.contracts.create(newContract)
                assertEquals(newContract, createdContract)
            }
        }
    }
}