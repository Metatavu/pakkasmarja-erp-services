package fi.metatavu.pakkasmarja.services.erp.test.functional.tests

import fi.metatavu.pakkasmarja.services.erp.test.functional.resources.LocalTestProfile
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile

/**
 * Tests for system resources
 *
 * @author Antti Leppä
 */
@QuarkusTest
@TestProfile(LocalTestProfile::class)
class NativeSystemResourceTest: SystemResourceTest() {

}