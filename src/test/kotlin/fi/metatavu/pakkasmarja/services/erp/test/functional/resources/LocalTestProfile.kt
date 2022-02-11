package fi.metatavu.pakkasmarja.services.erp.test.functional.resources

import io.quarkus.test.junit.QuarkusTestProfile

/**
 * Local Quarkus test profile
 */
class LocalTestProfile: QuarkusTestProfile {

    override fun getConfigOverrides(): Map<String, String> {
        val keycloakUrl = "http://localhost:8180"
        val keycloakRealm = "pakkasmarja"

        return mapOf(
            // "keycloak.url" to keycloakUrl,
            // "quarkus.oidc.auth-server-url" to "$keycloakUrl/realms/$keycloakRealm"
        )
    }

}