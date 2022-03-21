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
     * Mock SAP business partners
     *
     * @param ids business partner ids
     */
    fun mockBusinessPartners(vararg ids: String) {
        ids.forEach(this::mockBusinessPartner)
    }

    /**
     * Mock single SAP business partner
     *
     * @param id business partner id
     */
    private fun mockBusinessPartner(id: String) {
        requestMock("BusinessPartner", "sap/resources/businessPartners/$id.json")
    }

    /**
     * Mock SAP contracts
     *
     * @param ids contract ids
     */
    fun mockContracts(vararg ids: String) {
        ids.forEach(this::mockContract)
    }

    /**
     * Mock single SAP contract
     *
     * @param id contract id
     */
    private fun mockContract(id: String) {
        requestMock("BlanketAgreement", "sap/resources/contracts/$id.json")
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