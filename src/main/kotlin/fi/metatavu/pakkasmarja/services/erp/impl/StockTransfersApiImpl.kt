package fi.metatavu.pakkasmarja.services.erp.impl

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.metatavu.pakkasmarja.services.erp.api.model.SapStockTransfer
import fi.metatavu.pakkasmarja.services.erp.api.spec.StockTransfersApi
import fi.metatavu.pakkasmarja.services.erp.impl.translate.StockTransferTranslator
import fi.metatavu.pakkasmarja.services.erp.sap.StockTransfersController
import fi.metatavu.pakkasmarja.services.erp.sap.session.SapSessionController
import javax.annotation.security.RolesAllowed
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.Response

/**
 * Stock transfers API implementation
 */
@RequestScoped
@Transactional
@Suppress("unused")
@RolesAllowed(UserRole.INTEGRATION.name)
class StockTransfersApiImpl: StockTransfersApi, AbstractApi() {

    @Inject
    lateinit var stockTransfersController: StockTransfersController

    @Inject
    lateinit var stockTransferTranslator: StockTransferTranslator

    @Inject
    lateinit var sapSessionController: SapSessionController

    override fun createStockTransfer(sapStockTransfer: SapStockTransfer): Response {
        logger.info("Trying to create stock transfer from request body ${jacksonObjectMapper().writeValueAsString(sapStockTransfer)}")

        val result = sapSessionController.createSapSession().use { sapSession ->
            stockTransfersController.createStockTransfer(
                sapSession = sapSession,
                sapStockTransfer = sapStockTransfer
            )
        } ?: return createInternalServerError("Failed to create stock transfer")

        return createOk(stockTransferTranslator.translate(result))
    }

}