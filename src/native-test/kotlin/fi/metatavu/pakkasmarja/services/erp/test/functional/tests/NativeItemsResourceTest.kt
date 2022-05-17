package fi.metatavu.pakkasmarja.services.erp.test.functional.tests

import fi.metatavu.pakkasmarja.services.erp.test.functional.resources.LocalTestProfile
import fi.metatavu.pakkasmarja.services.erp.test.functional.resources.SapMockTestResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile

/**
 * Tests for items
 *
 * @author Antti Leppä
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(SapMockTestResource::class)
)
@TestProfile(LocalTestProfile::class)
class NativeItemsResourceTest: ItemsResourceTest() {

}