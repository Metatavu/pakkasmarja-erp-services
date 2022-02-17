package fi.metatavu.pakkasmarja.services.erp.test.functional.tests

import fi.metatavu.pakkasmarja.services.erp.test.functional.resources.LocalTestProfile
import io.quarkus.test.junit.QuarkusIntegrationTest
import io.quarkus.test.junit.TestProfile

/**
 * Native tests for system resources
 *
 * @author Antti Leppä
 */
@QuarkusIntegrationTest
@TestProfile(LocalTestProfile::class)
class NativeSystemResourceTest: SystemResourceTest() {

}
