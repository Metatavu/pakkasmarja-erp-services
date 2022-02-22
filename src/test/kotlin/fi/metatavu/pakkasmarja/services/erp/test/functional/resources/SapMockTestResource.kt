package fi.metatavu.pakkasmarja.services.erp.test.functional.resources

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager

/**
 * SAP-mocker for testing
 */
class SapMockTestResource: QuarkusTestResourceLifecycleManager {
    private val wireMockServer: WireMockServer = WireMockServer()

    override fun start(): Map<String, String> {
        wireMockServer.start()
        return mapOf(
            "fi.metatavu.pakkasmarja.sap-api-url" to wireMockServer.baseUrl()
        )
    }

    override fun stop() {
        wireMockServer.stop()
    }
}