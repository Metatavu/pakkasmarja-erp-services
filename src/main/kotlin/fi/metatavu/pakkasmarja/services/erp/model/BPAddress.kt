package fi.metatavu.pakkasmarja.services.erp.model


import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Data class for BPAddress
 *
 * @author Jari Nykänen
 */
data class BPAddress(
    @JsonProperty("AddressName")
    val addressName: String,
    @JsonProperty("AddressType")
    val addressType: String,
    @JsonProperty("Street")
    val street: String,
    @JsonProperty("ZipCode")
    val zipCode: String,
    @JsonProperty("City")
    val city: String
)