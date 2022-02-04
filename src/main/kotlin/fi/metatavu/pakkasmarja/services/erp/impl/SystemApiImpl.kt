package fi.metatavu.pakkasmarja.services.erp.impl

import fi.metatavu.pakkasmarja.services.erp.api.spec.SystemApi
import javax.enterprise.context.RequestScoped
import javax.ws.rs.Produces
import javax.ws.rs.core.Response

/**
 * System API implementation
 *
 * @author Antti Leppä
 */
@RequestScoped
class SystemApiImpl: SystemApi, fi.metatavu.pakkasmarja.services.erp.impl.AbstractApi()  {

    @Produces("application/json")
    override suspend fun ping(): Response {
        return createOk("pong")
    }

}
