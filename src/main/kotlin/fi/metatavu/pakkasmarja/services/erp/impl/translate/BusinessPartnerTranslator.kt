package fi.metatavu.pakkasmarja.services.erp.impl.translate

import com.fasterxml.jackson.databind.JsonNode
import fi.metatavu.pakkasmarja.services.erp.api.model.SapAddress
import fi.metatavu.pakkasmarja.services.erp.api.model.SapAddressType
import fi.metatavu.pakkasmarja.services.erp.api.model.SapBankAccount
import fi.metatavu.pakkasmarja.services.erp.api.model.SapBusinessPartner
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * Translator class for SAP business partners
 */
class BusinessPartnerTranslator: AbstractTranslator<JsonNode, SapBusinessPartner>() {
    /**
     * Translates a business partner from SAP into a format expected by spec
     *
     * @param entity business partner to be translated
     *
     * @return translated business partner
     */
    override fun translate(entity: JsonNode): SapBusinessPartner {
        return SapBusinessPartner(
            code = entity.get("CardCode").asText().toInt(),
            email = entity.get("Email").asText(),
            phoneNumbers = listOf( entity.get("Phone1").asText(), entity.get("Phone2").asText()),
            addresses = entity.get("BPAddresses").map(this::translateAddress),
            companyName = entity.get("CardName").asText(),
            federalTaxId = entity.get("FederalTaxID").asText(),
            vatLiable = translateVatLiable(entity.get("VatLiable").asText()),
            updated = getUpdatedDateTime(entity.get("UpdatedDate").asText(), entity.get("UpdatedTime").asText()),
            bankAccounts = entity.get("BPBankAccounts").map(this::translateBankAccount)
        )
    }

    /**
     * Combines UpdatedDate and UpdatedTime from SAP into a single OffsetDateTime-object
     *
     * @param updatedDate updated date
     * @param updatedTime updated time
     *
     * @return updated datetime
     */
    private fun getUpdatedDateTime(updatedDate: String, updatedTime: String): OffsetDateTime {
        val date = LocalDate.parse(updatedDate)
        val time = LocalTime.parse(updatedTime)
        return OffsetDateTime.of(date, time, ZoneOffset.of("Europe/Helsinki"))
    }

    /**
     * Translates a bank account from SAP into a format expected by spec
     *
     * @param bankAccount bank account to translate
     *
     * @return translated bank account
     */
    private fun translateBankAccount(bankAccount: JsonNode): SapBankAccount {
        return SapBankAccount(BIC = bankAccount.get("BICSwiftCode").asText(), IBAN = bankAccount.get("IBAN").asText())
    }

    /**
     * Translates vat liability info from SAP into a format expected by spec
     *
     * @param vatLiable vatLiable from SAP
     *
     * @return translated vatLiable
     */
    private fun translateVatLiable(vatLiable: String): SapBusinessPartner.VatLiable? {
        return when (vatLiable) {
            "vLiable" -> SapBusinessPartner.VatLiable.fI
            "vExempted" -> SapBusinessPartner.VatLiable.nOTLIABLE
            "vEC" -> SapBusinessPartner.VatLiable.eU
            else -> null
        }
    }

    /**
     * Translates an address from SAP into a format expected by spec
     *
     * @param address address from SAP
     *
     * @return translated address
     */
    private fun translateAddress(address: JsonNode): SapAddress {
        return SapAddress(
            type = resolveSapAddressType(address.get("AddressType").asText()),
            name = address.get("AddressName").asText(),
            streetAddress = address.get("Street").asText(),
            city = address.get("City").asText(),
            postalCode = address.get("ZipCode").asText(),
        )
    }

    /**
     * Resolves address types
     *
     * @param address address to be resolved
     *
     * @return resolved address type
     */
    private fun resolveSapAddressType(address: String): SapAddressType? {
        return when (address) {
            "bo_BillTo" -> SapAddressType.bILLING
            "bo_ShipTo" -> SapAddressType.dELIVERY
            else -> null
        }
    }
}