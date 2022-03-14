package fi.metatavu.pakkasmarja.services.erp.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Data class for BPBankAccount
 *
 * @author Jari Nykänen
 */
data class BPBankAccount(
    @JsonProperty("BICSwiftCode")
    val bICSwiftCode: String,
    @JsonProperty("IBAN")
    val iBAN: String
)