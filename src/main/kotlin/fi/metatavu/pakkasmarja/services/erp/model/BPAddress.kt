package fi.metatavu.pakkasmarja.services.erp.model


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * Data class for BPAddress
 *
 * @author Jari Nykänen
 */
@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
data class BPAddress(
    @JsonProperty("AddressName")
    val addressName: String?,
    @JsonProperty("AddressType")
    val addressType: String,
    @JsonProperty("Street")
    val street: String?,
    @JsonProperty("ZipCode")
    val zipCode: String?,
    @JsonProperty("City")
    val city: String?
)