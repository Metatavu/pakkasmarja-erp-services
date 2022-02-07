package fi.metatavu.pakkasmarja.services.erp.test.functional.tests

import fi.metatavu.pakkasmarja.services.erp.test.functional.TestBuilder
import fi.metatavu.pakkasmarja.services.erp.test.functional.resources.LocalTestProfile
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Tests for business partners
 *
 * @author Antti Leppä
 */
@QuarkusTest
@TestProfile(LocalTestProfile::class)
class BusinessPartnersResourceTest {

    /**
     * Tests list business partners
     */
    @Test
    fun listBusinessPartners() {
        TestBuilder().use {
            val listResult = it.manager.businessPartners.listBusinessPartners(
                updatedAfter = null,
                firstResult = null,
                maxResults = null
            )

            assertEquals(1, listResult.size)
            assertEquals(12345, listResult[0].code)
            assertEquals("fake@example.com", listResult[0].email)
            assertNull(listResult[0].addresses)
            assertNull(listResult[0].bankAccounts)
            assertNull(listResult[0].companyName)
            assertNull(listResult[0].federalTaxId)
            assertNull(listResult[0].phoneNumbers)
            assertNull(listResult[0].updated)
            assertNull(listResult[0].vatLiable)
        }
    }

}