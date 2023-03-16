package fi.metatavu.pakkasmarja.services.erp.test.functional.testcontainer

import org.slf4j.LoggerFactory
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer

/**
 * Test container for SAP mock service
 */
class SapMockTestContainer: GenericContainer<SapMockTestContainer>("metatavu/odata-mock:1.0.1") {

    private var edmPath: String? = null
    private var companyDb: String? = null
    private var username: String? = null
    private var password: String? = null
    private val logger = LoggerFactory.getLogger(SapMockTestContainer::class.java)

    init {
        withExposedPorts(8080)
        withLogConsumer {
            logger.info("SAP Mock: ${it.utf8String}")
        }
    }

    override fun configure() {
        val edmContainerPath = "/opt/edm.xml"
        withEnv("ODATA_MOCK_EDM_FILE", edmContainerPath)
        withEnv("ODATA_MOCK_SESSION_COOKIE_NAME", "B1SESSION")

        if (companyDb != null) {
            withEnv("ODATA_MOCK_SESSION_COMPANYDB", companyDb)
        }

        if (username != null) {
            withEnv("ODATA_MOCK_SESSION_USERNAME", username)
        }

        if (password != null) {
            withEnv("ODATA_MOCK_SESSION_PASSWORD", password)
        }

        withClasspathResourceMapping(edmPath, edmContainerPath, BindMode.READ_ONLY)
    }

    /**
     * Use the path to set EDM configuration file
     *
     * @param path path to configuration
     * @return self
     */
    fun withEdm(path: String): SapMockTestContainer {
        edmPath = path
        return self()
    }

    /**
     * User user
     *
     * @param companyDb company db
     * @param username username
     * @param password password
     * @return self
     */
    fun withUser(companyDb: String, username: String, password: String): SapMockTestContainer {
        this.companyDb = companyDb
        this.username = username
        this.password = password
        return self()
    }

}