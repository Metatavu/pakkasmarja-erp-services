package fi.metatavu.pakkasmarja.services.erp.test.functional.resources

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager

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


    /**
     * Returns resource from test resources
     *
     * @param name file name
     * @return resource as byte array
     */
    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun getResource(name: String): ByteArray {
        javaClass.classLoader.getResourceAsStream("sap-mock/${name}").use {
            return it.readBytes()
        }
    }
}