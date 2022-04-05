package fi.metatavu.pakkasmarja.services.erp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * Data class for GroupProperty
 *
 * @author Jari Nykänen
 */
@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
data class GroupProperty(
    @JsonProperty("code")
    val code: Int,
    @JsonProperty("property")
    val property: Int,
    @JsonProperty("isFrozen")
    val isFrozen: Boolean,
    @JsonProperty("isOrganic")
    val isOrganic: Boolean
)
