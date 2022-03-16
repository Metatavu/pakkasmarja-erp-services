package fi.metatavu.pakkasmarja.services.erp.test.functional.resources

import fi.metatavu.pakkasmarja.services.erp.test.functional.testcontainer.SapMockTestContainer
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager

/**
 * SAP-mocker for testing
 */
class SapMockTestResource: QuarkusTestResourceLifecycleManager {

    override fun start(): Map<String, String> {
        sapMockTestContainer
            .withEdm("edm.xml")
            .withUser(companyDb = companyDb, username = username, password = password)
            .start()

        val testContainerHost = sapMockTestContainer.host.toString()
        val testContainerPort = sapMockTestContainer.getMappedPort(8080).toString()
        val odataMockUrl = "http://$testContainerHost:$testContainerPort"

        return mapOf(
            "fi.metatavu.pakkasmarja.odata-mock-url" to odataMockUrl,
            "fi.metatavu.pakkasmarja.sap-api-url" to "$odataMockUrl/odata",
            "fi.metatavu.pakkasmarja.sap-company-db" to companyDb,
            "fi.metatavu.pakkasmarja.sap-user-name" to username,
            "fi.metatavu.pakkasmarja.sap-user-password" to password
        )
    }

    override fun stop() {
        sapMockTestContainer.stop()
    }

    companion object {
        var sapMockTestContainer: SapMockTestContainer = SapMockTestContainer()
        val companyDb = "companydb"
        val username = "sapuser"
        val password = "sappass"
    }

}