package fi.metatavu.pakkasmarja.services.erp.config

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.metatavu.pakkasmarja.services.erp.model.GroupCode
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
     *
     * @return group codes file in JSON format
     */
    fun getGroupCodesFile(): List<GroupCode> {
        val file = File(groupCodesFileName)
        return jacksonObjectMapper().readValue(file, Array<GroupCode>::class.java).asList()
    }

}