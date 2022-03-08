package fi.metatavu.pakkasmarja.services.erp.test.functional.tests

import fi.metatavu.pakkasmarja.services.erp.test.functional.resources.LocalTestProfile
import fi.metatavu.pakkasmarja.services.erp.test.functional.resources.SapMockTestResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.*

/**
 * Tests for items
 *
 * TODO: Add support for invalid data testing
 *
 * @author Jari Nykänen
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(SapMockTestResource::class)
)
@TestProfile(LocalTestProfile::class)
class ItemsResourceTest: AbstractResourceTest() {

    /**
     * Test listing items
     */
    @Test
    fun testListItems() {
        createTestBuilder().use {
            val items = it.manager.items.list(
                itemGroupCode = 102,
                updatedAfter = getTestDate(),
                firstResult = null,
                maxResults = null
            )
            assertEquals(1, items.size)

            val allItems = it.manager.items.list(
                itemGroupCode = null,
                updatedAfter = getTestDate(),
                firstResult = null,
                maxResults = null
            )
            assertEquals(3, allItems.size)

            val emptyList = it.manager.items.list(
                itemGroupCode = 9999,
                updatedAfter = null,
                firstResult = null,
                maxResults = null
            )
            assertEquals(0, emptyList.size)

            it.invalidAccess.items.assertListFailStatus(
                expectedStatus = 401,
                itemGroupCode = 102,
                updatedAfter = null,
                firstResult = null,
                maxResults = null
            )

            it.nullAccess.items.assertListFailStatus(
                expectedStatus = 401,
                itemGroupCode = 102,
                updatedAfter = null,
                firstResult = null,
                maxResults = null
            )
        }
    }

    /**
     * Test finding items
     */
    @Test
    fun testFindItems() {
        createTestBuilder().use {
            val foundItem = it.manager.items.find(sapId = 1)
            assertNotNull(foundItem)
            assertEquals(1, foundItem.code)
            assertEquals(100, foundItem.itemGroupCode)

            it.manager.items.assertFindFailStatus(expectedStatus = 404, sapId = 9999)
            it.nullAccess.items.assertFindFailStatus(expectedStatus = 401, sapId = 1)
            it.invalidAccess.items.assertFindFailStatus(expectedStatus = 401, sapId = 1)
        }
    }

    /**
     * Get offset date-time for tests
     *
     * @return offset date-time in string format
     */
    private fun getTestDate(): String {
        val dateFilter = LocalDate.of(2022, 3, 17)
        val timeFilter = LocalTime.of(10, 0, 0)
        val zone = ZoneId.of("Europe/Helsinki")
        val zoneOffset = zone.rules.getOffset(LocalDateTime.now())
        val updatedAfter = OffsetDateTime.of(dateFilter, timeFilter, zoneOffset)

        return updatedAfter.toString()
    }
}