package fi.metatavu.pakkasmarja.services.erp.test.functional.tests

import fi.metatavu.pakkasmarja.services.erp.test.functional.TestBuilder
import io.quarkus.test.common.DevServicesContext
import io.quarkus.test.junit.QuarkusTest
import org.eclipse.microprofile.config.ConfigProvider
import java.time.*

/**
 * Abstract base class for resource tests
 */
@QuarkusTest
abstract class AbstractResourceTest {

    private var devServicesContext: DevServicesContext? = null

    /**
     * Creates new test builder
     *
     * @return new test builder
     */
    protected fun createTestBuilder(): TestBuilder {
        return TestBuilder(getConfig())
    }

    /**
     * Construct offset date time string
     *
     * @param dateFilter local date filter
     * @param timeFilter local time filter
     * @return constructed string
     */
    protected fun toOffsetDateTime(dateFilter: LocalDate, timeFilter: LocalTime): String {
        val zone = ZoneId.of("Europe/Helsinki")
        val zoneOffset = zone.rules.getOffset(LocalDateTime.now())
        val updatedAfter = OffsetDateTime.of(dateFilter, timeFilter, zoneOffset)

        return updatedAfter.toString()
    }

    /**
     * Returns config for tests.
     *
     * If tests are running in native mode, method returns config from devServicesContext and
     * when tests are running in JVM mode method returns config from the Quarkus config
     *
     * @return config for tests
     */
    private fun getConfig(): Map<String, String> {
        return getDevServiceConfig() ?: getQuarkusConfig()
    }

    /**
     * Returns test config from dev services
     *
     * @return test config from dev services
     */
    private fun getDevServiceConfig(): Map<String, String>? {
        return devServicesContext?.devServicesProperties()
    }

    /**
     * Returns test config from Quarkus
     *
     * @return test config from Quarkus
     */
    private fun getQuarkusConfig(): Map<String, String> {
        val config = ConfigProvider.getConfig()
        return config.propertyNames.associateWith { config.getConfigValue(it).rawValue }
    }

}