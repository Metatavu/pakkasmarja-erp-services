package fi.metatavu.pakkasmarja.services.erp.sap

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.metatavu.pakkasmarja.services.erp.api.model.SapStockTransfer
import fi.metatavu.pakkasmarja.services.erp.api.model.SapStockTransferLine
import fi.metatavu.pakkasmarja.services.erp.api.model.SapStockTransferLineBinAllocation
import fi.metatavu.pakkasmarja.services.erp.model.BinActionType
import fi.metatavu.pakkasmarja.services.erp.model.StockTransfer
import fi.metatavu.pakkasmarja.services.erp.model.StockTransferLine
import fi.metatavu.pakkasmarja.services.erp.model.StockTransferLinesBinAllocation
import fi.metatavu.pakkasmarja.services.erp.sap.session.SapSession
import java.time.format.DateTimeFormatter
import javax.enterprise.context.ApplicationScoped

/**
 * Controller for SAP stock transfers
 */
@ApplicationScoped
class StockTransfersController: AbstractSapResourceController<StockTransfer>() {

    /**
     * Creates a new stock transfer
     *
     * @param sapSession SAP session
     * @param sapStockTransfer a stock transfer to be created
     * @return created stock transfer
     */
    fun createStockTransfer(
        sapSession: SapSession,
        sapStockTransfer: SapStockTransfer
    ): StockTransfer? {
        val resourceUrl = "${sapSession.apiUrl}/StockTransfers"
        val objectMapper = jacksonObjectMapper()
        val payload = objectMapper.writeValueAsString(buildNewStockTransfer(sapStockTransfer))

        return try {
            createSapEntity(
                targetClass = StockTransfer::class.java,
                item = payload,
                resourceUrl = resourceUrl,
                sessionId = sapSession.sessionId,
                routeId = sapSession.routeId
            )
        } catch (e: Exception) {
            logger.error("Failed to create stock transfer", e)
            null
        }
    }

    /**
     * Builds a new stock transfer
     *
     * @param sapStockTransfer a stock transfer to be created
     * @return new stock transfer
     */
    private fun buildNewStockTransfer(sapStockTransfer: SapStockTransfer): StockTransfer {
        return StockTransfer(
            docDate = DateTimeFormatter.ISO_DATE.format(sapStockTransfer.docDate),
            cardCode = sapStockTransfer.businessPartnerCode.toString(),
            comments = sapStockTransfer.comments,
            salesPersonCode = sapStockTransfer.salesPersonCode,
            fromWarehouse = sapStockTransfer.fromWarehouse,
            toWarehouse = sapStockTransfer.toWarehouse,
            stockTransferLines = sapStockTransfer.lines.map {
                this.buildNewStockTransferLine(
                    sapStockTransfer = sapStockTransfer,
                    line = it
                )
            }
        )
    }

    /**
     * Builds a new stock transfer line
     *
     * @param sapStockTransfer stock transfer
     * @param line stock transfer line
     * @return new stock transfer line
     */
    private fun buildNewStockTransferLine(
        sapStockTransfer: SapStockTransfer,
        line: SapStockTransferLine
    ): StockTransferLine {
        return StockTransferLine(
            itemCode = line.itemCode.toString(),
            quantity = line.quantity,
            warehouseCode = sapStockTransfer.toWarehouse,
            fromWarehouseCode = sapStockTransfer.fromWarehouse,
            stockTransferLinesBinAllocations = line.binAllocations.map {
                this.buildNewStockTransferLineBinAllocation(
                    sapStockTransfer = it,
                    line = line
                )
            }
        )
    }

    /**
     * Creates a new stock transfer line bin allocation
     *
     * @param sapStockTransfer a stock transfer
     * @param line a stock transfer line
     * @return created stock transfer line bin allocation
     */
    private fun buildNewStockTransferLineBinAllocation(
        sapStockTransfer: SapStockTransferLineBinAllocation,
        line: SapStockTransferLine
    ): StockTransferLinesBinAllocation {
        val actionType = when (sapStockTransfer.actionType) {
            fi.metatavu.pakkasmarja.services.erp.api.model.BinActionType.TO_WAREHOUSE -> BinActionType.batToWarehouse
            fi.metatavu.pakkasmarja.services.erp.api.model.BinActionType.FROM_WAREHOUSE -> BinActionType.batFromWarehouse
        }

        return StockTransferLinesBinAllocation(
            binAbsEntry = sapStockTransfer.absEntry,
            binActionType = actionType,
            quantity = line.quantity
        )
    }

}