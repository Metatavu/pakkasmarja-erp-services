package fi.metatavu.pakkasmarja.services.erp.test.functional.tests

import fi.metatavu.pakkasmarja.services.erp.test.functional.resources.LocalTestProfile
import fi.metatavu.pakkasmarja.services.erp.test.functional.resources.SapMockTestResource
import fi.metatavu.pakkasmarja.services.erp.test.functional.sap.SapMock
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
            SapMock().use { sapMock ->

                sapMock.mockItems("1", "2", "3")

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

               val oneItem = it.manager.items.list(
                    itemGroupCode = null,
                    updatedAfter = getTestDate(),
                    firstResult = null,
                    maxResults = 1
                )
                assertEquals(1, oneItem.size)

                val lastItem = it.manager.items.list(
                    itemGroupCode = null,
                    updatedAfter = getTestDate(),
                    firstResult = 1,
                    maxResults = null
                )
                assertEquals(2, lastItem.size)
                assertEquals(2, lastItem[0].code)
                assertEquals(3, lastItem[1].code)

                val filterFirstAndMax = it.manager.items.list(
                    itemGroupCode = null,
                    updatedAfter = getTestDate(),
                    firstResult = 2,
                    maxResults = 1
                )
                assertEquals(1, filterFirstAndMax.size)
                assertEquals(3, filterFirstAndMax[0].code)

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
    }

    /**
     * Test finding items
     */
    @Test
    fun testFindItems() {
        createTestBuilder().use {
            SapMock().use { sapMock ->
                sapMock.mockItems("1", "2", "3")

                val foundItem = it.manager.items.find(sapId = 1)
                assertNotNull(foundItem)
                assertEquals(1, foundItem.code)
                assertEquals(100, foundItem.itemGroupCode)

                it.manager.items.assertFindFailStatus(expectedStatus = 404, sapId = 9999)
                it.nullAccess.items.assertFindFailStatus(expectedStatus = 401, sapId = 1)
                it.invalidAccess.items.assertFindFailStatus(expectedStatus = 401, sapId = 1)
            }
        }
    }

    /**
     * Test finding items
     */
    @Test
    fun testFindItemGroup105() {
        createTestBuilder().use {
            SapMock().use { sapMock ->
                sapMock.mockItems("4")

                val foundItem = it.manager.items.find(sapId = 4)
                assertNotNull(foundItem)
                assertEquals(4, foundItem.code)
                assertEquals(105, foundItem.itemGroupCode)
            }
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

        return toOffsetDateTime(dateFilter, timeFilter)
    }

}