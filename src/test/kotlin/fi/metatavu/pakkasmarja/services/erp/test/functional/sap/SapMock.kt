package fi.metatavu.pakkasmarja.services.erp.test.functional.sap

import fi.metatavu.pakkasmarja.services.erp.test.sap.mock.apis.EntriesApi
import fi.metatavu.pakkasmarja.services.erp.test.sap.mock.models.Entry
import org.eclipse.microprofile.config.ConfigProvider
import java.io.InputStreamReader

/**
 * SAP mock client
 */
class SapMock: AutoCloseable {

    private val sapMockUrl: String
        get() {
            val config = ConfigProvider.getConfig()
            return config.getValue("fi.metatavu.pakkasmarja.odata-mock-url", String::class.java)
        }

    override fun close() {
        EntriesApi(basePath = sapMockUrl).deleteEntries(name = null)
    }

    /**
     * Mock SAP items
     *
     * @param ids item ids
     */
    fun mockItems(vararg ids: String) {
        ids.forEach(this::mockItem)
    }

    /**
     * Mock single SAP item
     *
     * @param id item id
     */
    private fun mockItem(id: String) {
        requestMock("Item", "sap/resources/items/$id.json")
    }

    /**
     * Request mock from OData mock server
     *
     * @param entryName entry name
     * @param resourcePath resource path
     * @return mock entry
     */
    private fun requestMock(entryName: String, resourcePath: String): Entry {
        val data = getResource(resourcePath)
        return EntriesApi(basePath = sapMockUrl).createEntry(Entry(name = entryName, data = data))
    }

    /**
     * Reads resource from class path
     *
     * @param resourcePath resource path
     * @return resource as string
     */
    private fun getResource(resourcePath: String): String {
        javaClass.classLoader.getResourceAsStream(resourcePath).use { resourceStream ->
            InputStreamReader(resourceStream!!).use { inputStreamReader ->
                return inputStreamReader.readText()
            }
        }
    }
}