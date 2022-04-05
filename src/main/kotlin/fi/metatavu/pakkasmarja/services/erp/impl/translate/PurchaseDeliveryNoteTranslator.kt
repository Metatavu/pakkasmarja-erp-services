package fi.metatavu.pakkasmarja.services.erp.impl.translate

import fi.metatavu.pakkasmarja.services.erp.api.model.*
import fi.metatavu.pakkasmarja.services.erp.model.BatchNumber
import fi.metatavu.pakkasmarja.services.erp.model.PurchaseDeliveryNote
import fi.metatavu.pakkasmarja.services.erp.model.PurchaseDeliveryNoteLine
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
        val docDate = LocalDate.from(DateTimeFormatter.ISO_DATE.parse(sapEntity.docDate))

        return SapPurchaseDeliveryNote(
            docDate = docDate,
            businessPartnerCode = sapEntity.cardCode.toInt(),
            salesPersonCode = sapEntity.salesPersonCode,
            comments = sapEntity.comments,
            lines = sapEntity.documentLines.map {
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
            itemCode = line.itemCode.toInt(),
            quantity = line.quantity,
            unitPrice = line.unitPrice,
            warehouseCode = line.warehouseCode,
            batchNumbers = line.batchNumbers.map(this::translateBatchNumber)
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
            batchNumber = batchNumber.batchNumberProperty,
            quantity = batchNumber.quantity
        )
    }

}