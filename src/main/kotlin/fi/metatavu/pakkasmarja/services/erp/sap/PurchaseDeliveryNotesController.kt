package fi.metatavu.pakkasmarja.services.erp.sap

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.metatavu.pakkasmarja.services.erp.api.model.SapBatchNumber
import fi.metatavu.pakkasmarja.services.erp.api.model.SapPurchaseDeliveryNote
import fi.metatavu.pakkasmarja.services.erp.api.model.SapPurchaseDeliveryNoteLine
import fi.metatavu.pakkasmarja.services.erp.model.BatchNumber
import fi.metatavu.pakkasmarja.services.erp.model.PurchaseDeliveryNote
import fi.metatavu.pakkasmarja.services.erp.model.PurchaseDeliveryNoteLine
import fi.metatavu.pakkasmarja.services.erp.sap.session.SapSession
import java.time.format.DateTimeFormatter
import javax.enterprise.context.ApplicationScoped

/**
 * Controller for SAP purchase delivery notes
 */
@ApplicationScoped
class PurchaseDeliveryNotesController: AbstractSapResourceController<PurchaseDeliveryNote>() {

    /**
     * Creates a new purchase delivery note
     *
     * @param sapSession SAP session
     * @param sapPurchaseDeliveryNote a purchase delivery note to be created
     * @return created purchase delivery note
     */
    fun createPurchaseDeliveryNote(
        sapSession: SapSession,
        sapPurchaseDeliveryNote: SapPurchaseDeliveryNote
    ): PurchaseDeliveryNote? {
        val resourceUrl = "${sapSession.apiUrl}/PurchaseDeliveryNotes"
        val objectMapper = jacksonObjectMapper()
        val payload = objectMapper.writeValueAsString(buildNewPurchaseDeliveryNote(sapPurchaseDeliveryNote))

        return try {
            createSapEntity(
                targetClass = PurchaseDeliveryNote::class.java,
                item = payload,
                resourceUrl = resourceUrl,
                sessionId = sapSession.sessionId,
                routeId = sapSession.routeId
            )
        } catch (e: Exception) {
            logger.error("Failed to create purchase delivery note", e)
            null
        }
    }

    /**
     * Builds a new purchase delivery note
     *
     * @param sapPurchaseDeliveryNote a purchase delivery note to be created
     * @return new purchase delivery note
     */
    private fun buildNewPurchaseDeliveryNote(sapPurchaseDeliveryNote: SapPurchaseDeliveryNote): PurchaseDeliveryNote {
        return PurchaseDeliveryNote(
            docObjectCode = "oPurchaseDeliveryNotes",
            docDate = DateTimeFormatter.ISO_DATE.format(sapPurchaseDeliveryNote.docDate),
            cardCode = sapPurchaseDeliveryNote.businessPartnerCode.toString(),
            comments = sapPurchaseDeliveryNote.comments,
            salesPersonCode = sapPurchaseDeliveryNote.salesPersonCode,
            documentLines = sapPurchaseDeliveryNote.lines.map(this::buildNewPurchaseDeliveryNoteLine)
        )
    }

    /**
     * Builds a new purchase delivery note line
     *
     * @param line purchase delivery note line
     * @return new purchase delivery note line
     */
    private fun buildNewPurchaseDeliveryNoteLine(line: SapPurchaseDeliveryNoteLine): PurchaseDeliveryNoteLine {
        return PurchaseDeliveryNoteLine(
            itemCode = line.itemCode.toString(),
            quantity = line.quantity,
            unitPrice = line.unitPrice,
            warehouseCode = line.warehouseCode,
            batchNumbers = line.batchNumbers.map(this::buildNewBatchNumber)
        )
    }

    /**
     * Builds a new batch number
     *
     * @param batchNumber batch number
     * @return new batch number
     */
    fun buildNewBatchNumber(batchNumber: SapBatchNumber): BatchNumber {
        return BatchNumber(
            batchNumber = batchNumber.batchNumber,
            quantity = batchNumber.quantity
        )
    }
}


