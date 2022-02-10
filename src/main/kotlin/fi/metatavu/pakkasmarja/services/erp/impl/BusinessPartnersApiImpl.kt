package fi.metatavu.pakkasmarja.services.erp.impl

import fi.metatavu.pakkasmarja.services.erp.api.model.SapBusinessPartner
import fi.metatavu.pakkasmarja.services.erp.api.spec.BusinessPartnersApi
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
class BusinessPartnersApiImpl: BusinessPartnersApi, fi.metatavu.pakkasmarja.services.erp.impl.AbstractApi()  {

    @Inject
    private lateinit var businessPartnersController: fi.metatavu.pakkasmarja.services.erp.sap.BusinessPartnersController

    override suspend fun listBusinessPartners(
        updatedAfter: OffsetDateTime?,
        firstResult: Int?,
        maxResults: Int?
    ): Response {
        return createOk(listOf(
            SapBusinessPartner(
                code = 12345,
                email = "fake@example.com"
            )
        ))
    }

}
