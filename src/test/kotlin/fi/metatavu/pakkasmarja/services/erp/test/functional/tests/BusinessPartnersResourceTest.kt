package fi.metatavu.pakkasmarja.services.erp.test.functional.tests

import fi.metatavu.pakkasmarja.services.erp.test.client.models.SapAddressType
import fi.metatavu.pakkasmarja.services.erp.test.client.models.SapBusinessPartner
import fi.metatavu.pakkasmarja.services.erp.test.functional.resources.LocalTestProfile
import fi.metatavu.pakkasmarja.services.erp.test.functional.resources.SapMockTestResource
import fi.metatavu.pakkasmarja.services.erp.test.functional.sap.SapMock
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.*

/**
 * Tests for business partners
 *
 * TODO: Add support for invalid data testing
 *
 * @author Antti Leppä
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(SapMockTestResource::class)
)
@TestProfile(LocalTestProfile::class)
class BusinessPartnersResourceTest: AbstractResourceTest() {

    /**
     * Tests list business partners
     */
    @Test
    fun testListBusinessPartners() {
        createTestBuilder().use {
            SapMock().use { sapMock ->
                sapMock.mockBusinessPartners("1", "2", "3")

                val businessPartners = it.manager.businessPartners.listBusinessPartners(updatedAfter = getTestDate())
                assertEquals(3, businessPartners.size)

                val partner = businessPartners.find{ sapBusinessPartner -> sapBusinessPartner.vatLiable == SapBusinessPartner.VatLiable.EU }!!
                assertEquals(3, partner.code)
                assertEquals("jorma@example.com", partner.email)

                val phoneNumbers = partner.phoneNumbers
                assertNotNull(phoneNumbers)
                assertEquals(2, phoneNumbers!!.size)
                assertEquals("0440120122", phoneNumbers[0])
                assertEquals("0440120123", phoneNumbers[1])

                val addresses = partner.addresses
                assertNotNull(addresses)
                assertEquals(1, addresses!!.size)
                val address = addresses[0]
                assertEquals(SapAddressType.DELIVERY, address.type)
                assertEquals("Home", address.name)
                assertEquals("Mikkeli", address.city)
                assertEquals("Hallituskatu 7", address.streetAddress)
                assertEquals("50100", address.postalCode)

                assertEquals("MetaLab", partner.companyName)
                assertEquals("0000000", partner.federalTaxId)

                assertEquals(getTestValidationDate(), partner.updated)

                val bankAccounts = partner.bankAccounts
                assertNotNull(bankAccounts)
                assertEquals(1, bankAccounts!!.size)

                val bankAccount = bankAccounts[0]
                assertEquals("FI61000000000", bankAccount.iban)
                assertEquals("SBANFIHH", bankAccount.bic)

                val withLegacyCode = businessPartners.find { sapBusinessPartner -> sapBusinessPartner.code == 3 }
                assertNotNull(withLegacyCode)
                assertEquals(3, withLegacyCode?.code)
                assertEquals(12345, withLegacyCode?.legacyCode)

                it.invalidAccess.businessPartners.assertListFailStatus(expectedStatus = 401, updatedAfter = getTestDate())
                it.nullAccess.businessPartners.assertListFailStatus(expectedStatus = 401, updatedAfter = getTestDate())
            }

        }
    }

    /**
     * Get offset date-time for tests
     *
     * @return offset date-time in string format
     */
    private fun getTestDate(): String {
        val dateFilter = LocalDate.of(2022, 2, 17)
        val timeFilter = LocalTime.of(10, 0, 0)

        return toOffsetDateTime(dateFilter, timeFilter)
    }

    /**
     * Get offset date-time for test date validation
     *
     * @return offset date-time in string format
     */
    private fun getTestValidationDate(): String {
        val dateFilter = LocalDate.of(2022, 2, 18)
        val timeFilter = LocalTime.of(8, 0, 12)

        return toOffsetDateTime(dateFilter, timeFilter)
    }
}