package fi.metatavu.pakkasmarja.services.erp.test.functional

import fi.metatavu.jaxrs.test.functional.builder.AbstractAccessTokenTestBuilder
import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder
import fi.metatavu.jaxrs.test.functional.builder.auth.*
import fi.metatavu.pakkasmarja.services.erp.test.client.infrastructure.ApiClient
import fi.metatavu.pakkasmarja.services.erp.test.functional.impl.auth.TestBuilderAuthentication
import java.net.URL

/**
 * Abstract test builder class
 *
 * @param config config
 * @author Jari Nykänen
 * @author Antti Leppä
 */
class TestBuilder(private val config: Map<String, String>): AbstractAccessTokenTestBuilder<ApiClient>() {
    val manager = createTestBuilderAuthentication(username = "manager", password = "test")
    val nullAccess = TestBuilderAuthentication(this, NullAccessTokenProvider())
    val invalidAccess = TestBuilderAuthentication(this, InvalidAccessTokenProvider())

    override fun createTestBuilderAuthentication(
        abstractTestBuilder: AbstractTestBuilder<ApiClient, AccessTokenProvider>,
        authProvider: AccessTokenProvider
    ): AuthorizedTestBuilderAuthentication<ApiClient, AccessTokenProvider> {
        return TestBuilderAuthentication(this, authProvider)
    }

    /**
     * Creates test builder authenticatior for given user
     *
     * @param username username
     * @param password password
     * @return test builder authenticatior for given user
     */
    private fun createTestBuilderAuthentication(username: String, password: String): TestBuilderAuthentication {
        val serverUrl = getKeycloakUrl()
        val realm = getKeycloakRealm()
        val clientId = "test"
        return TestBuilderAuthentication(this, KeycloakAccessTokenProvider(serverUrl, realm, clientId, username, password, null))
    }

    /**
     * Returns Keycloak URL
     *
     * @return Keycloak URL
     */
    private fun getKeycloakUrl(): String {
        return config["keycloak.url"]!!
    }

    /**
     * Returns Keycloak realm
     *
     * @return Keycloak realm
     */
    private fun getKeycloakRealm(): String {
        val serverUrl = URL(getKeycloakServerUrl())
        val pattern = Regex("(/realms/)([a-z]*)")
        val match = pattern.find(serverUrl.path)!!
        val (_, realm) = match.destructured
        return realm
    }

    /**
     * Returns Keycloak server URL
     *
     * @return Keycloak server URL
     */
    private fun getKeycloakServerUrl(): String {
        return config["quarkus.oidc.auth-server-url"]!!
    }

}