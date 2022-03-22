package fi.metatavu.pakkasmarja.services.erp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Data class for Contract
 *
 * @author Jari Nykänen
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Contract(
    @JsonProperty("StartDate")
    val startDate: String,
    @JsonProperty("EndDate")
    val endDate: String,
    @JsonProperty("DocNum")
    val docNum: Int,
    @JsonProperty("BPCode")
    val bpCode: String,
    @JsonProperty("ContactPersonCode")
    val contactPersonCode: Int,
    @JsonProperty("Status")
    var status: String,
    @JsonProperty("SigningDate")
    val signingDate: String,
    @JsonProperty("TerminateDate")
    val terminateDate: String? = null,
    @JsonProperty("Remarks")
    val remarks: String,
    @JsonProperty("AgreementNo")
    val agreementNo: Int? = null,
    @JsonProperty("BlanketAgreements_ItemsLines")
    val contractLines: List<ContractLine>
)