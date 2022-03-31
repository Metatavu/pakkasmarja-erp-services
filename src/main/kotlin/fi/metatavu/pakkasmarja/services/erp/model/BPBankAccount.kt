package fi.metatavu.pakkasmarja.services.erp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * Data class for BPBankAccount
 *
 * @author Jari Nykänen
 */
@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
data class BPBankAccount(
    @JsonProperty("BICSwiftCode")
    val bicSwiftCode: String,
    @JsonProperty("IBAN")
    val iban: String
)