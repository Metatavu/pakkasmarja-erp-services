package fi.metatavu.pakkasmarja.services.erp.test.functional.tests

import fi.metatavu.pakkasmarja.services.erp.test.functional.TestBuilder
import io.quarkus.test.common.DevServicesContext

/**
 * Abstract base class for resource tests
 */
abstract class AbstractResourceTest {

    private lateinit var devServicesContext: DevServicesContext

    /**
     * Creates new test builder
     *
     * @return new test builder
     */
    protected fun createTestBuilder(): TestBuilder {
        return TestBuilder(devServicesContext.devServicesProperties())
    }

}