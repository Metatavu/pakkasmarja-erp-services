package fi.metatavu.pakkasmarja.services.erp.impl

import fi.metatavu.pakkasmarja.services.erp.api.spec.BusinessPartnersApi
import fi.metatavu.pakkasmarja.services.erp.impl.translate.BusinessPartnerTranslator
import fi.metatavu.pakkasmarja.services.erp.sap.BusinessPartnersController
import io.quarkus.security.Authenticated
import java.time.OffsetDateTime
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.Response

/**
 * Business partners API implementation
 *
 * @author Antti Leppä
 */
@RequestScoped
@Transactional
class BusinessPartnersApiImpl: BusinessPartnersApi, AbstractApi() {

    @Inject
    private lateinit var businessPartnersController: BusinessPartnersController

    @Inject
    private lateinit var businessPartnerTranslator: BusinessPartnerTranslator

    @Authenticated
    override fun listBusinessPartners(
        updatedAfter: OffsetDateTime?,
        firstResult: Int?,
        maxResults: Int?
    ): Response {
        val businessPartners = businessPartnersController.listBusinessPartners(
            updatedAfter = updatedAfter,
            firstResult = firstResult,
            maxResults = maxResults
        )

        val partners = businessPartners.map(businessPartnerTranslator::translate)

        if (partners.find { partner -> partner == null } != null) {
            return createInternalServerError("Failed to translate a SAP-item")
        }

        return createOk(partners)
    }
}
