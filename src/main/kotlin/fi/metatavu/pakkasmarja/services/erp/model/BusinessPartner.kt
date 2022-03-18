package fi.metatavu.pakkasmarja.services.erp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Data class for BusinessPartner
 *
 * @author Jari Nykänen
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class BusinessPartner(
    @JsonProperty("CardCode")
    val cardCode: String,
    @JsonProperty("CardName")
    val cardName: String,
    @JsonProperty("Phone1")
    val phone1: String?,
    @JsonProperty("Phone2")
    val phone2: String?,
    @JsonProperty("VatLiable")
    val vatLiable: String,
    @JsonProperty("FederalTaxID")
    val federalTaxID: String,
    @JsonProperty("EmailAddress")
    val emailAddress: String?,
    @JsonProperty("CardForeignName")
    val cardForeignName: String?,
    @JsonProperty("BPAddresses")
    val bPAddresses: List<BPAddress>,
    @JsonProperty("BPBankAccounts")
    val bPBankAccounts: List<BPBankAccount>,
    @JsonProperty("UpdateDate")
    val updateDate: String,
    @JsonProperty("UpdateTime")
    val updateTime: String
)