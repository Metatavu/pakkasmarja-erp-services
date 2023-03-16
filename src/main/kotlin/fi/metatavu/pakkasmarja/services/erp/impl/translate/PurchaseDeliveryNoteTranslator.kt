package fi.metatavu.pakkasmarja.services.erp.impl.translate

import fi.metatavu.pakkasmarja.services.erp.api.model.*
import fi.metatavu.pakkasmarja.services.erp.model.BatchNumber
import fi.metatavu.pakkasmarja.services.erp.model.PurchaseDeliveryNote
import fi.metatavu.pakkasmarja.services.erp.model.PurchaseDeliveryNoteLine
import fi.metatavu.pakkasmarja.services.erp.sap.exception.SapTranslationException
import javax.enterprise.context.ApplicationScoped

/**
 * The translator class for SAP purchase delivery note
 */
@ApplicationScoped
class PurchaseDeliveryNoteTranslator: AbstractTranslator<PurchaseDeliveryNote, SapPurchaseDeliveryNote>() {

    /**
     * Translates a contract from SAP into tbe format expected by spec
     *
     * @param sapEntity a contract from SAP
     * @return translated contract
     */
    override fun translate(sapEntity: PurchaseDeliveryNote): SapPurchaseDeliveryNote {
        val docDate = resolveLocalDate(sapEntity.getDocDate())

        return SapPurchaseDeliveryNote(
            docDate = docDate ?: throw SapTranslationException("Failed to parse docDate from SAP"),
            businessPartnerCode = sapEntity.getCardCode()?.toInt() ?: 0,
            salesPersonCode = sapEntity.getSalesPersonCode() ?: 0,
            comments = sapEntity.getComments(),
            lines = sapEntity.getDocumentLines().map {
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
    private fun translateLine(line: PurchaseDeliveryNoteLine): SapPurchaseDeliveryNoteLine {
        return SapPurchaseDeliveryNoteLine(
            itemCode = line.getItemCode()?.toInt() ?: 0,
            quantity = line.getQuantity() ?: 0.0,
            unitPrice = line.getUnitPrice() ?: 0.0,
            warehouseCode = line.getWarehouseCode() ?: "",
            batchNumbers = line.getBatchNumbers().map(this::translateBatchNumber)
        )
    }

    /**
     * Translates a batch number from SAP into tbe format expected by spec
     *
     * @param batchNumber batch number from SAP
     * @return translated batch number
     */
    private fun translateBatchNumber(batchNumber: BatchNumber): SapBatchNumber {
        return SapBatchNumber(
            batchNumber = batchNumber.batchNumber,
            quantity = batchNumber.quantity
        )
    }

}