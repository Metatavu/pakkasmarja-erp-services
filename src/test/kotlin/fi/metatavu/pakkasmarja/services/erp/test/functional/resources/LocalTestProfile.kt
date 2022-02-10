package fi.metatavu.pakkasmarja.services.erp.test.functional.resources

import io.quarkus.test.junit.QuarkusTestProfile

/**
 * Local Quarkus test profile
 */
class LocalTestProfile: QuarkusTestProfile {

    override fun getConfigOverrides(): Map<String, String> {
        return mapOf(
            "quarkus.keycloak.devservices.realm-name" to "pakkasmarja",
            "quarkus.keycloak.devservices.realm-path" to "kc.json"
        )
    }

}