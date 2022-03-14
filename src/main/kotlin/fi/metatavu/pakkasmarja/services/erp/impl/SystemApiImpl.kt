package fi.metatavu.pakkasmarja.services.erp.impl

import fi.metatavu.pakkasmarja.services.erp.api.spec.SystemApi
import javax.enterprise.context.RequestScoped
import javax.ws.rs.core.Response

/**
 * System API implementation
 *
 * @author Antti Leppä
 */
@RequestScoped
@Suppress("unused")
class SystemApiImpl: SystemApi, AbstractApi()  {

    override fun ping(): Response {
        return createOk("pong")
    }

}