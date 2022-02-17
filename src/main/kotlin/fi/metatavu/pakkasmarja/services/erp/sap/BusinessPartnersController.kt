package fi.metatavu.pakkasmarja.services.erp.sap

import com.fasterxml.jackson.databind.JsonNode
import fi.metatavu.pakkasmarja.services.erp.sap.session.SapSessionController
import fi.metatavu.pakkasmarja.services.erp.sap.utils.SapUtils
import java.time.OffsetDateTime
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * The controller for business partners
 */
@ApplicationScoped
class BusinessPartnersController {
    @Inject
    private lateinit var sapSessionController: SapSessionController

    @Inject
    private lateinit var sapUtils: SapUtils

    /**
     * Lists business partners
     *
     * @param updatedAfter "updated after"-filter
     * @param firstResult first result
     * @param maxResults max results
     *
     * @return business partners
     */
    fun listBusinessPartners(updatedAfter: OffsetDateTime?, firstResult: Int?, maxResults: Int?): ArrayList<JsonNode> {
        sapSessionController.createSapSession().use { sapSession ->
            val resourceUrl = "${sapSession.apiUrl}/BusinessPartners"
            val updatedAfterFilter = updatedAfter?.let { "and ${sapUtils.createdUpdatedAfterFilter(it)}" } ?: ""
            val filter = "\$filter=(CardType eq 'cSupplier' $updatedAfterFilter)"
            val select = "\$select=CardCode,CardType,CardName,CardForeignName,Phone1,Phone2,EmailAddress,BPAddresses,BPBankAccounts,FederalTaxID,VatLiable";

            return sapUtils.getItemsAsJsonNodes(
                resourceUrl = resourceUrl,
                filter = filter,
                select = select,
                routeId = sapSession.routeId,
                sessionId = sapSession.sessionId
            )
        }
    }
}