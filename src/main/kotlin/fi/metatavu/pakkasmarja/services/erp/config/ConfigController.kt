package fi.metatavu.pakkasmarja.services.erp.config

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import fi.metatavu.pakkasmarja.services.erp.model.GroupProperty
import javax.enterprise.context.ApplicationScoped

/**
 * Controller for configurations
 */
@ApplicationScoped
class ConfigController {

    /**
     * Returns the group properties from config file
     *
     * @return list of group properties
     */
    fun getGroupPropertiesFromConfigFile(): List<GroupProperty> {
        val groupCodes = javaClass.classLoader.getResourceAsStream("group-codes.json") ?: return emptyList()
        return jacksonObjectMapper().readValue(groupCodes)
    }

}