package fi.metatavu.pakkasmarja.services.erp.impl.translate

import fi.metatavu.pakkasmarja.services.erp.api.model.SapAddress
import fi.metatavu.pakkasmarja.services.erp.api.model.SapAddressType
import fi.metatavu.pakkasmarja.services.erp.api.model.SapBankAccount
import fi.metatavu.pakkasmarja.services.erp.api.model.SapBusinessPartner
import fi.metatavu.pakkasmarja.services.erp.model.BPAddress
import fi.metatavu.pakkasmarja.services.erp.model.BPBankAccount
import fi.metatavu.pakkasmarja.services.erp.model.BusinessPartner
import javax.enterprise.context.ApplicationScoped

/**
 * The translator class for SAP business partners
 */
@ApplicationScoped
class BusinessPartnerTranslator: AbstractTranslator<BusinessPartner, SapBusinessPartner>() {

    /**
     * Translates a business partner from SAP into the format expected by spec
     *
     * @param node business partner to be translated
     * @return translated business partner
     */
    override fun translate(node: BusinessPartner): SapBusinessPartner {
        return SapBusinessPartner(
            code = node.cardCode.toInt(),
            email = node.emailAddress ?: "",
            phoneNumbers = listOf(node.phone1 ?: "", node.phone2 ?: ""),
            addresses = node.bPAddresses.map(this::translateAddress),
            companyName = node.cardName,
            federalTaxId = node.federalTaxID,
            vatLiable = translateVatLiable(node.vatLiable),
            updated = getUpdatedDateTime(node.updateDate, node.updateTime),
            bankAccounts = node.bPBankAccounts.map(this::translateBankAccount)
        )
    }

    /**
     * Translates a bank account from SAP into the format expected by spec
     *
     * @param bankAccount bank account to translate
     * @return translated bank account
     */
    private fun translateBankAccount(bankAccount: BPBankAccount): SapBankAccount {
        return SapBankAccount(BIC = bankAccount.bICSwiftCode, IBAN = bankAccount.iBAN)
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
    private fun translateAddress(address: BPAddress): SapAddress {
        return SapAddress(
            type = resolveSapAddressType(address.addressType),
            name = address.addressName,
            streetAddress = address.street,
            city = address.city,
            postalCode = address.zipCode,
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