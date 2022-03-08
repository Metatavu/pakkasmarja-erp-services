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
class BusinessPartnerTranslator: AbstractTranslator<JsonNode, SapBusinessPartner>() {

    @Inject
    lateinit var logger: Logger

    /**
     * Translates a business partner from SAP into the format expected by spec
     *
     * @param node business partner to be translated
     * @return translated business partner
     */
    override fun translate(node: JsonNode): SapBusinessPartner {
        return SapBusinessPartner(
            code = node.get("CardCode").asText().toInt(),
            email = node.get("Email").asText(),
            phoneNumbers = listOf(node.get("Phone1").asText(), node.get("Phone2").asText()),
            addresses = node.get("BPAddresses").map(this::translateAddress),
            companyName = node.get("CardName").asText(),
            federalTaxId = node.get("FederalTaxID").asText(),
            vatLiable = translateVatLiable(node.get("VatLiable").asText()),
            updated = getUpdatedDateTime(node.get("UpdatedDate").asText(), node.get("UpdatedTime").asText()),
            bankAccounts = node.get("BPBankAccounts").map(this::translateBankAccount)
        )
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