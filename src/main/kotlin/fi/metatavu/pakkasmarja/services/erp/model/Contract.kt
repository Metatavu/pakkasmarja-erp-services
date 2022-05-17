package fi.metatavu.pakkasmarja.services.erp.model

import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.quarkus.runtime.annotations.RegisterForReflection


/**
 * Model class for Contract
 *
 * @author Jari Nykänen
 * @author Antti Leppä
 */
@RegisterForReflection
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class Contract() {

    @JsonProperty("StartDate")
    private var startDate: String? = null

    @JsonProperty("EndDate")
    private var endDate: String? = null
    
    @JsonProperty("DocNum")
    private var docNum: Int? = null

    @JsonProperty("BPCode")
    private var bpCode: String? = null

    @JsonProperty("ContactPersonCode")
    private var contactPersonCode: Int? = null

    @JsonProperty("Status")
    private var status: String? = null

    @JsonProperty("SigningDate")
    private var signingDate: String? = null

    @JsonProperty("TerminateDate")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private var terminateDate: String? = null

    @JsonProperty("Remarks")
    private var remarks: String? = null

    @JsonProperty("AgreementNo")
    private var agreementNo: Int? = null

    @JsonProperty("BlanketAgreements_ItemsLines")
    private var contractLines: List<ContractLine> = listOf()

    private var additionalFields: MutableMap<String, Any?> = mutableMapOf()

    constructor(
        startDate: String?,
        endDate: String?,
        docNum: Int?,
        bpCode: String?,
        contactPersonCode: Int?,
        status: String?,
        signingDate: String?,
        terminateDate: String?,
        remarks: String?,
        agreementNo: Int?,
        contractLines: List<ContractLine>,
        additionalFields: MutableMap<String, Any?>
    ) : this() {
        this.startDate = startDate
        this.endDate = endDate
        this.docNum = docNum
        this.bpCode = bpCode
        this.contactPersonCode = contactPersonCode
        this.status = status
        this.signingDate = signingDate
        this.terminateDate = terminateDate
        this.remarks = remarks
        this.agreementNo = agreementNo
        this.contractLines = contractLines
        this.additionalFields = additionalFields
    }

    fun getStartDate(): String? {
        return startDate
    }

    fun setStartDate(startDate: String?) {
        this.startDate = startDate
    }

    fun getEndDate(): String? {
        return endDate
    }

    fun setEndDate(endDate: String?) {
        this.endDate = endDate
    }

    fun getDocNum(): Int? {
        return docNum
    }

    fun setDocNum(docNum: Int?) {
        this.docNum = docNum
    }

    fun getBpCode(): String? {
        return bpCode
    }

    fun setBpCode(bpCode: String?) {
        this.bpCode = bpCode
    }

    fun getContactPersonCode(): Int? {
        return contactPersonCode
    }

    fun setContactPersonCode(contactPersonCode: Int?) {
        this.contactPersonCode = contactPersonCode
    }

    fun getStatus(): String? {
        return status
    }

    fun setStatus(status: String?) {
        this.status = status
    }

    fun getSigningDate(): String? {
        return signingDate
    }

    fun setSigningDate(signingDate: String?) {
        this.signingDate = signingDate
    }

    fun getTerminateDate(): String? {
        return terminateDate
    }

    fun setTerminateDate(terminateDate: String?) {
        this.terminateDate = terminateDate
    }

    fun getRemarks(): String? {
        return remarks
    }

    fun setRemarks(remarks: String?) {
        this.remarks = remarks
    }

    fun getAgreementNo(): Int? {
        return agreementNo
    }

    fun setAgreementNo(agreementNo: Int?) {
        this.agreementNo = agreementNo
    }

    fun getContractLines(): List<ContractLine> {
        return contractLines
    }

    fun setContractLines(contractLines: List<ContractLine>) {
        this.contractLines = contractLines
    }

    @JsonAnyGetter
    fun getAdditionalFields(): MutableMap<String, Any?> {
        return additionalFields
    }

    @JsonAnySetter
    fun setAdditionalFields(name: String, value: Any?) {
        this.additionalFields[name] = value
    }

}