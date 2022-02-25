package fi.metatavu.pakkasmarja.services.erp.impl.translate

import com.fasterxml.jackson.databind.JsonNode
import fi.metatavu.pakkasmarja.services.erp.api.model.SapAddress
import fi.metatavu.pakkasmarja.services.erp.api.model.SapAddressType
import fi.metatavu.pakkasmarja.services.erp.api.model.SapBankAccount
import fi.metatavu.pakkasmarja.services.erp.api.model.SapBusinessPartner
import org.jboss.logging.Logger
import java.time.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * The translator class for SAP business partners
 */
@ApplicationScoped
class BusinessPartnerTranslator() {

    @Inject
    private lateinit var logger: Logger

    /**
     * Translates a business partner from SAP into the format expected by spec
     *
     * @param businessPartner business partner to be translated
     * @return translated business partner
     */
    fun translate(businessPartner: JsonNode): SapBusinessPartner? {
        return try {
            SapBusinessPartner(
                code = businessPartner.get("CardCode").asText().toInt(),
                email = businessPartner.get("Email").asText(),
                phoneNumbers = listOf(businessPartner.get("Phone1").asText(), businessPartner.get("Phone2").asText()),
                addresses = businessPartner.get("BPAddresses").map(this::translateAddress),
                companyName = businessPartner.get("CardName").asText(),
                federalTaxId = businessPartner.get("FederalTaxID").asText(),
                vatLiable = translateVatLiable(businessPartner.get("VatLiable").asText()),
                updated = getUpdatedDateTime(businessPartner.get("UpdatedDate").asText(), businessPartner.get("UpdatedTime").asText()),
                bankAccounts = businessPartner.get("BPBankAccounts").map(this::translateBankAccount)
            )
        } catch (e: Exception) {
            logger.error("Failed to translate a business partner from SAP: ${e.message}")
            null
        }

    }

    /**
     * Combines UpdatedDate and UpdatedTime from SAP into a single OffsetDateTime-object
     *
     * @param updatedDate updated date
     * @param updatedTime updated time
     * @return updated datetime
     */
    private fun getUpdatedDateTime(updatedDate: String, updatedTime: String): OffsetDateTime {
        val date = LocalDate.parse(updatedDate)
        val time = LocalTime.parse(updatedTime)
        val zone = ZoneId.of("Europe/Helsinki")
        val zoneOffset = zone.rules.getOffset(LocalDateTime.now())
        return OffsetDateTime.of(date, time, zoneOffset)
    }

    /**
     * Translates a bank account from SAP into the format expected by spec
     *
     * @param bankAccount bank account to translate
     * @return translated bank account
     */
    private fun translateBankAccount(bankAccount: JsonNode): SapBankAccount {
        return SapBankAccount(BIC = bankAccount.get("BICSwiftCode").asText(), IBAN = bankAccount.get("IBAN").asText())
    }

    /**
     * Translates vat liability info from SAP into the format expected by spec
     *
     * @param vatLiable vatLiable from SAP
     * @return translated vatLiable
     */
    private fun translateVatLiable(vatLiable: String): SapBusinessPartner.VatLiable? {
        return when (vatLiable) {
            "vLiable" -> SapBusinessPartner.VatLiable.FI
            "vExempted" -> SapBusinessPartner.VatLiable.NOT_LIABLE
            "vEC" -> SapBusinessPartner.VatLiable.EU
            else -> null
        }
    }

    /**
     * Translates an address from SAP into the format expected by spec
     *
     * @param address address from SAP
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
     * @return resolved address type
     */
    private fun resolveSapAddressType(address: String): SapAddressType? {
        return when (address) {
            "bo_BillTo" -> SapAddressType.BILLING
            "bo_ShipTo" -> SapAddressType.DELIVERY
            else -> null
        }
    }
}