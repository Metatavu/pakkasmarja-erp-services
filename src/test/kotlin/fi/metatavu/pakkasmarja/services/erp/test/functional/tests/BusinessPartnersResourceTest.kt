package fi.metatavu.pakkasmarja.services.erp.test.functional.tests

import fi.metatavu.pakkasmarja.services.erp.test.client.models.SapAddressType
import fi.metatavu.pakkasmarja.services.erp.test.client.models.SapBusinessPartner
import fi.metatavu.pakkasmarja.services.erp.test.functional.resources.LocalTestProfile
import fi.metatavu.pakkasmarja.services.erp.test.functional.resources.SapMockTestResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.*

/**
 * Tests for business partners
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
            val dateFilter = LocalDate.of(2022, 2, 17)
            val timeFilter = LocalTime.of(10, 0, 0)
            val zone = ZoneId.of("Europe/Helsinki")
            val zoneOffset = zone.rules.getOffset(LocalDateTime.now())
            val updatedAfter = OffsetDateTime.of(dateFilter, timeFilter, zoneOffset)
            val businessPartners = it.manager.businessPartners.listBusinessPartners(updatedAfter = updatedAfter.toString(), firstResult = null, maxResults = null)
            assertEquals(3, businessPartners.size)

            val partner = businessPartners.find{ sapBusinessPartner -> sapBusinessPartner.vatLiable == SapBusinessPartner.VatLiable.EU }!!
            assertEquals(1, partner.code)
            assertEquals("jorma@jorman.com", partner.email)

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
            assertEquals( "Home", address.name)
            assertEquals("Mikkeli", address.city)
            assertEquals("Hallituskatu 7", address.streetAddress)
            assertEquals("50100", address.postalCode)

            assertEquals("MetaLab", partner.companyName)
            assertEquals("0000000", partner.federalTaxId)

            val expectedDate = LocalDate.of(2022, 2, 18)
            val expectedTime = LocalTime.of(8, 0, 12)
            val expectedDateTime = OffsetDateTime.of(expectedDate, expectedTime, zoneOffset)
            assertEquals(expectedDateTime.toString(), partner.updated)

            val bankAccounts = partner.bankAccounts
            assertNotNull(bankAccounts)
            assertEquals(1, bankAccounts!!.size)

            val bankAccount = bankAccounts[0]
            assertEquals("FI61000000000", bankAccount.IBAN)
            assertEquals("SBANFIHH", bankAccount.BIC)
        }
    }
}