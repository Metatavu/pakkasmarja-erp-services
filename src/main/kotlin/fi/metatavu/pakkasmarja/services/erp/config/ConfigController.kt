package fi.metatavu.pakkasmarja.services.erp.config

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.io.File
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for configurations
 */
@ApplicationScoped
class ConfigController {
    @Inject
    @ConfigProperty(name="fi.metatavu.pakkasmarja.group-codes-file-name")
    lateinit var groupCodesFileName: String

    /**
     * Returns the group codes config file in JSON format
     * @return group codes file in JSON format
     */
    fun getGroupCodesFile(): JsonNode {
        val file = File(groupCodesFileName)
        return ObjectMapper().readTree(file)
    }
}