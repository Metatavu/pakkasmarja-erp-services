package fi.metatavu.pakkasmarja.services.erp.impl

import fi.metatavu.pakkasmarja.services.erp.api.spec.BusinessPartnersApi
import fi.metatavu.pakkasmarja.services.erp.impl.translate.BusinessPartnerTranslator
import fi.metatavu.pakkasmarja.services.erp.sap.BusinessPartnersController
import fi.metatavu.pakkasmarja.services.erp.sap.session.SapSessionController
import java.time.OffsetDateTime
import javax.annotation.security.RolesAllowed
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
@Suppress("unused")
@RolesAllowed(UserRole.INTEGRATION.name)
class BusinessPartnersApiImpl: BusinessPartnersApi, AbstractApi() {

    @Inject
    lateinit var businessPartnersController: BusinessPartnersController

    @Inject
    lateinit var businessPartnerTranslator: BusinessPartnerTranslator

    @Inject
    lateinit var sapSessionController: SapSessionController

    override fun listBusinessPartners(updatedAfter: OffsetDateTime?): Response {
        val businessPartners = sapSessionController.createSapSession().use { sapSession ->
            businessPartnersController.listBusinessPartners(
                sapSession = sapSession,
                updatedAfter = updatedAfter
            )
        }

        return createOk(businessPartners.map(businessPartnerTranslator::translate))
    }

}