package fi.metatavu.pakkasmarja.services.erp.sap

import com.fasterxml.jackson.databind.JsonNode
import fi.metatavu.pakkasmarja.services.erp.model.BusinessPartner
import fi.metatavu.pakkasmarja.services.erp.sap.session.SapSessionController
import java.time.OffsetDateTime
import javax.enterprise.context.RequestScoped
import javax.inject.Inject

/**
 * The controller for business partners
 */
@RequestScoped
class BusinessPartnersController: AbstractSapResourceController() {

    @Inject
    lateinit var sapSessionController: SapSessionController

    /**
     * Lists business partners
     *
     * @param updatedAfter "updated after"-filter
     * @return business partners
     */
    fun listBusinessPartners(updatedAfter: OffsetDateTime?): List<BusinessPartner> {
        sapSessionController.createSapSession().use { sapSession ->
            val resourceUrl = "${sapSession.apiUrl}/BusinessPartners"

            var updatedAfterFilter = ""
            if (updatedAfter != null) {
                updatedAfterFilter = "and ${createdUpdatedAfterFilter(updatedAfter)}"
            }

            val filter = "\$filter=(CardType eq 'cSupplier' $updatedAfterFilter)"
            val select = "\$select=CardCode,CardType,CardName,Phone1,Phone2,EmailAddress,BPAddresses,BPBankAccounts,FederalTaxID,VatLiable,UpdateDate,UpdateTime"

            val requestUrl = constructSAPRequestUrl(
                baseUrl = resourceUrl,
                select = select,
                filter = filter,
                firstResult = null
            )

            val businessPartners = sapListRequest(
                requestUrl = requestUrl,
                sapSession = sapSession,
                maxResults = null
            )

            return businessPartners.map(this::convertToModel)
        }
    }
}