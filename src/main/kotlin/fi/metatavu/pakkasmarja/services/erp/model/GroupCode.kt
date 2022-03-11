package fi.metatavu.pakkasmarja.services.erp.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Data class for GroupCode
 */
data class GroupCode(
    @JsonProperty("code")
    val code: Int,
    @JsonProperty("itemGroupPropertyName")
    val itemGroupPropertyName: String,
    @JsonProperty("isFrozen")
    val isFrozen: Boolean,
    @JsonProperty("isOrganic")
    val isOrganic: Boolean
)
