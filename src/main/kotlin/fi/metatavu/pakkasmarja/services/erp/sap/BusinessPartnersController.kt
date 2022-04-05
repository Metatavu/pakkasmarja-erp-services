package fi.metatavu.pakkasmarja.services.erp.sap

import fi.metatavu.pakkasmarja.services.erp.model.BusinessPartner
import fi.metatavu.pakkasmarja.services.erp.sap.session.SapSession
import java.time.OffsetDateTime
import javax.enterprise.context.ApplicationScoped

/**
 * The controller for business partners
 */
@ApplicationScoped
class BusinessPartnersController: AbstractSapResourceController<BusinessPartner>() {

    /**
     * Lists business partners
     *
     * @param sapSession SAP session
     * @param updatedAfter "updated after"-filter
     * @return business partners
     */
    fun listBusinessPartners(
        sapSession: SapSession,
        updatedAfter: OffsetDateTime?
    ): List<BusinessPartner> {
        val resourceUrl = "${sapSession.apiUrl}/BusinessPartners"

        var updatedAfterFilter = ""
        if (updatedAfter != null) {
            updatedAfterFilter = "and ${createdUpdatedAfterFilter(updatedAfter)}"
        }

        val filter = "\$filter=(CardType eq SAPB1.BoCardTypes'cSupplier' $updatedAfterFilter)"
        val select = "\$select=CardCode,CardType,CardName,Phone1,Phone2,EmailAddress,BPAddresses,BPBankAccounts,FederalTaxID,VatLiable,UpdateDate,UpdateTime"

        val requestUrl = constructSAPRequestUrl(
            baseUrl = resourceUrl,
            select = select,
            filter = filter,
            firstResult = null,
            maxResults = null
        )

        return sapListRequest(
            targetClass = BusinessPartner::class.java,
            requestUrl = requestUrl,
            sapSession = sapSession,
        ) ?: return emptyList()
    }

}