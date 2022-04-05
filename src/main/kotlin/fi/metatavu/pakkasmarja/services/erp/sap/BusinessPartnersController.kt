package fi.metatavu.pakkasmarja.services.erp.sap

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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
        val select = "\$select=*"

        val requestUrl = constructSAPRequestUrl(
            baseUrl = resourceUrl,
            select = select,
            filter = filter,
            firstResult = null,
            maxResults = null
        )

        val result = sapListRequest(
            targetClass = BusinessPartner::class.java,
            requestUrl = requestUrl,
            sapSession = sapSession,
        )

        println("result:" + jacksonObjectMapper().writeValueAsString(result))

        return result
    }

}