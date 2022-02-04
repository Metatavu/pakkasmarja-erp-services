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
     * Tests list Examples
     */
    @Test
    fun listBusinessPartners() {
        TestBuilder().use {
            val listResult = it.manager.businessPartners.listBusinessPartners(
                updatedAfter = null,
                firstResult = null,
                maxResults = null
            )

            assertEquals(0, listResult.size)
        }
    }

}