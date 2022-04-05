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
     * @param sapEntity business partner to be translated
     * @return translated business partner
     */
    override fun translate(sapEntity: BusinessPartner): SapBusinessPartner {
        val phoneNumbers = mutableListOf<String>()

        sapEntity.getPhone1()?.let { phoneNumbers.add(it) }
        sapEntity.getPhone2()?.let { phoneNumbers.add(it) }

        return SapBusinessPartner(
            code = sapEntity.getCardCode()!!.toInt(),
            email = sapEntity.getEmailAddress() ?: "",
            phoneNumbers = phoneNumbers,
            addresses = sapEntity.getBPAddresses().map(this::translateAddress),
            companyName = sapEntity.getCardName(),
            federalTaxId = sapEntity.getFederalTaxID(),
            vatLiable = translateVatLiable(sapEntity.getVatLiable()),
            updated = getUpdatedDateTime(sapEntity.getUpdateDate()!!, sapEntity.getUpdateTime()!!),
            bankAccounts = sapEntity.getBPBankAccounts().map(this::translateBankAccount),
            legacyCode = sapEntity.getLegCardCode()
        )
    }

    /**
     * Translates a bank account from SAP into the format expected by spec
     *
     * @param bankAccount bank account to translate
     * @return translated bank account
     */
    private fun translateBankAccount(bankAccount: BPBankAccount): SapBankAccount {
        return SapBankAccount(bic = bankAccount.bicSwiftCode, iban = bankAccount.iban)
    }

    /**
     * Translates vat liability info from SAP into the format expected by spec
     *
     * @param vatLiable vatLiable from SAP
     * @return translated vatLiable
     */
    private fun translateVatLiable(vatLiable: String?): SapBusinessPartner.VatLiable? {
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