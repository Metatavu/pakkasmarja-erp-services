package fi.metatavu.pakkasmarja.services.erp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * Data class for BusinessPartner
 *
 * @author Jari Nykänen
 */
@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
data class BusinessPartner(
    @JsonProperty("CardCode")
    val cardCode: String,
    @JsonProperty("CardName")
    val cardName: String?,
    @JsonProperty("Phone1")
    val phone1: String?,
    @JsonProperty("Phone2")
    val phone2: String?,
    @JsonProperty("VatLiable")
    val vatLiable: String?,
    @JsonProperty("FederalTaxID")
    val federalTaxID: String?,
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
    val updateTime: String,
    @JsonProperty("U_PFZ_LegCardCode")
    val legCardCode: Int?
)