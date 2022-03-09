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
@Suppress("unused")
@Authenticated
class BusinessPartnersApiImpl: BusinessPartnersApi, AbstractApi() {

    @Inject
    lateinit var businessPartnersController: BusinessPartnersController

    @Inject
    lateinit var businessPartnerTranslator: BusinessPartnerTranslator

    override fun listBusinessPartners(updatedAfter: OffsetDateTime?): Response {
        val businessPartners = businessPartnersController.listBusinessPartners(updatedAfter = updatedAfter)
        val translatedBusinessPartners = businessPartners.map(businessPartnerTranslator::translate)

        return createOk(translatedBusinessPartners)
    }

}
