package fi.metatavu.pakkasmarja.services.erp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * Model class for BusinessPartner
 */
@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
class BusinessPartner() {

    @JsonProperty("CardCode")
    private var cardCode: String? = null

    @JsonProperty("CardName")
    private var cardName: String? = null

    @JsonProperty("Phone1")
    private var phone1: String? = null

    @JsonProperty("Phone2")
    private var phone2: String? = null

    @JsonProperty("VatLiable")
    private var vatLiable: String? = null

    @JsonProperty("FederalTaxID")
    private var federalTaxID: String? = null

    @JsonProperty("EmailAddress")
    private var emailAddress: String? = null

    @JsonProperty("CardForeignName")
    private var cardForeignName: String? = null

    @JsonProperty("BPAddresses")
    private var bPAddresses: List<BPAddress> = emptyList()

    @JsonProperty("BPBankAccounts")
    private var bPBankAccounts: List<BPBankAccount> = emptyList()

    @JsonProperty("UpdateDate")
    private var updateDate: String? = null

    @JsonProperty("UpdateTime")
    private var updateTime: String? = null

    @JsonProperty("U_PFZ_LegCardCode")
    private var legCardCode: Int? = null

    fun getCardCode(): String? {
        return cardCode
    }

    fun setCardCode(value: String?) {
        this.cardCode = value
    }

    fun getCardName(): String? {
        return cardName
    }

    fun getPhone1(): String? {
        return phone1
    }

    fun getPhone2(): String? {
        return phone2
    }

    fun getVatLiable(): String? {
        return vatLiable
    }

    fun getFederalTaxID(): String? {
        return federalTaxID
    }

    fun getEmailAddress(): String? {
        return emailAddress
    }

    fun getCardForeignName(): String? {
        return cardForeignName
    }

    fun getBPAddresses(): List<BPAddress> {
        return bPAddresses
    }

    fun getBPBankAccounts(): List<BPBankAccount> {
        return bPBankAccounts
    }

    fun getUpdateDate(): String? {
        return updateDate
    }

    fun getUpdateTime(): String? {
        return updateTime
    }

    fun getLegCardCode(): Int? {
        return legCardCode
    }

    fun setLegCardCode(value: Int?) {
        this.legCardCode = value
    }

    fun setCardName(value: String?) {
        this.cardName = value
    }

    fun setPhone1(value: String?) {
        this.phone1 = value
    }

    fun setPhone2(value: String?) {
        this.phone2 = value
    }

    fun setVatLiable(value: String?) {
        this.vatLiable = value
    }

    fun setFederalTaxID(value: String?) {
        this.federalTaxID = value
    }

    fun setEmailAddress(value: String?) {
        this.emailAddress = value
    }

    fun setCardForeignName(value: String?) {
        this.cardForeignName = value
    }

    fun setBPAddresses(value: List<BPAddress>) {
        this.bPAddresses = value
    }

    fun setBPBankAccounts(value: List<BPBankAccount>) {
        this.bPBankAccounts = value
    }

    fun setUpdateDate(value: String?) {
        this.updateDate = value
    }

    fun setUpdateTime(value: String?) {
        this.updateTime = value
    }

}