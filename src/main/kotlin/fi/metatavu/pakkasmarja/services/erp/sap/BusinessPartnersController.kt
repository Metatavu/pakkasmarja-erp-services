package fi.metatavu.pakkasmarja.services.erp.sap

import com.fasterxml.jackson.databind.JsonNode
import fi.metatavu.pakkasmarja.services.erp.sap.session.SapSessionController
import java.time.OffsetDateTime
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * The controller for business partners
 */
@ApplicationScoped
class BusinessPartnersController: AbstractSapResourceController() {

    @Inject
    lateinit var sapSessionController: SapSessionController

    /**
     * Lists business partners
     *
     * @param updatedAfter "updated after"-filter
     * @param firstResult first result
     * @param maxResults max results
     * @return business partners
     */
    fun listBusinessPartners(updatedAfter: OffsetDateTime?, firstResult: Int?, maxResults: Int?): List<JsonNode> {
        sapSessionController.createSapSession().use { sapSession ->
            val resourceUrl = "${sapSession.apiUrl}/BusinessPartners"

            var updatedAfterFilter = ""
            if (updatedAfter != null) {
                updatedAfterFilter = "and ${createdUpdatedAfterFilter(updatedAfter)}"
            }

            val filter = "\$filter=(CardType eq 'cSupplier' $updatedAfterFilter)"
            val select = "\$select=CardCode,CardType,CardName,Phone1,Phone2,EmailAddress,BPAddresses,BPBankAccounts,FederalTaxID,VatLiable,UpdateDate,UpdateTime";

            return getDataFromSap(
                resourceUrl = resourceUrl,
                filter = filter,
                select = select,
                routeId = sapSession.routeId,
                sessionId = sapSession.sessionId
            )
        }
    }
}