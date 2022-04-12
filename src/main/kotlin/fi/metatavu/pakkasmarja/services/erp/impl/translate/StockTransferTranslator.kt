package fi.metatavu.pakkasmarja.services.erp.impl.translate

import fi.metatavu.pakkasmarja.services.erp.api.model.BinActionType
import fi.metatavu.pakkasmarja.services.erp.api.model.SapStockTransfer
import fi.metatavu.pakkasmarja.services.erp.api.model.SapStockTransferLine
import fi.metatavu.pakkasmarja.services.erp.api.model.SapStockTransferLineBinAllocation
import fi.metatavu.pakkasmarja.services.erp.model.StockTransfer
import fi.metatavu.pakkasmarja.services.erp.model.StockTransferLine
import fi.metatavu.pakkasmarja.services.erp.model.StockTransferLinesBinAllocation
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.enterprise.context.ApplicationScoped

/**
 * The translator class for SAP contracts
 */
@ApplicationScoped
class StockTransferTranslator: AbstractTranslator<StockTransfer, SapStockTransfer>() {

    /**
     * Translates a contract from SAP into tbe format expected by spec
     *
     * @param sapEntity a contract from SAP
     * @return translated contract
     */
    override fun translate(sapEntity: StockTransfer): SapStockTransfer {
        val docDate = LocalDate.from(DateTimeFormatter.ISO_DATE.parse(sapEntity.getDocDate()))

        return SapStockTransfer(
            docDate = docDate,
            businessPartnerCode = sapEntity.getCardCode()?.toInt() ?: 0,
            salesPersonCode = sapEntity.getSalesPersonCode() ?: 0,
            fromWarehouse = sapEntity.getFromWarehouse() ?: "",
            toWarehouse = sapEntity.getToWarehouse() ?: "",
            comments = sapEntity.getComments(),
            lines = sapEntity.getStockTransferLines().map {
                this.translateLine(
                    line = it
                )
            }
        )
    }

    /**
     * Translates a line from SAP into tbe format expected by spec
     *
     * @param line line from SAP
     * @return translated line
     */
    private fun translateLine(line: StockTransferLine): SapStockTransferLine {
        return SapStockTransferLine(
            itemCode = line.getItemCode()?.toInt() ?: 0,
            quantity = line.getQuantity() ?: 0.0,
            binAllocations = line.getStockTransferLinesBinAllocations().map(this::translateBinAllocation)
        )
    }

    /**
     * Translates a bin allocation from SAP into tbe format expected by spec
     *
     * @param sapEntity bin allocation from SAP
     * @return translated bin allocation
     */
    private fun translateBinAllocation(sapEntity: StockTransferLinesBinAllocation): SapStockTransferLineBinAllocation {
        val actionType = when (sapEntity.getBinActionType()!!) {
            fi.metatavu.pakkasmarja.services.erp.model.BinActionType.batFromWarehouse -> BinActionType.FROM_WAREHOUSE
            fi.metatavu.pakkasmarja.services.erp.model.BinActionType.batToWarehouse -> BinActionType.TO_WAREHOUSE
        }

        return SapStockTransferLineBinAllocation(
            absEntry = sapEntity.getBinAbsEntry() ?: 0,
            actionType = actionType
        )
    }

}