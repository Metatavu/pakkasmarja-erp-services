package fi.metatavu.pakkasmarja.services.erp.test.functional.resources

import io.quarkus.test.junit.QuarkusTestProfile

/**
 * Local Quarkus test profile
 */
class LocalTestProfile: QuarkusTestProfile {
    override fun getConfigOverrides(): Map<String, String> {
        val config: MutableMap<String, String> = HashMap()

        config["fi.metatavu.pakkasmarja.sap-company-db"] = "TestDB"
        config["fi.metatavu.pakkasmarja.sap-user-name"] = "Jorma"
        config["fi.metatavu.pakkasmarja.sap-user-password"] = "jormankoira"

        return config
    }
}