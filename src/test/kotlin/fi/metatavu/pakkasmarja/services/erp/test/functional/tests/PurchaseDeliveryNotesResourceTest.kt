package fi.metatavu.pakkasmarja.services.erp.test.functional.tests

import fi.metatavu.pakkasmarja.services.erp.test.client.models.*
import fi.metatavu.pakkasmarja.services.erp.test.functional.resources.LocalTestProfile
import fi.metatavu.pakkasmarja.services.erp.test.functional.resources.SapMockTestResource
import fi.metatavu.pakkasmarja.services.erp.test.functional.sap.SapMock
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * Tests for purchase delivery notes
 *
 * TODO: Add support for invalid data testing
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(SapMockTestResource::class)
)
@TestProfile(LocalTestProfile::class)
class PurchaseDeliveryNotesResourceTest: AbstractResourceTest() {

    /**
     * Tests creating a purchase delivery note
     */
    @Test
    fun testCreatePurchaseDeliveryNote() {
        createTestBuilder().use {
            SapMock().use { sapMock ->
                sapMock.mockItems("1")
                sapMock.mockBusinessPartners("1")

                val purchaseDeliveryNotePayload = SapPurchaseDeliveryNote(
                    docDate = "2022-04-01",
                    businessPartnerCode = 1,
                    salesPersonCode = 123,
                    lines = arrayOf(
                        SapPurchaseDeliveryNoteLine(
                            itemCode = 1,
                            quantity = 50.0,
                            unitPrice = 15.0,
                            warehouseCode = "10",
                            batchNumbers = arrayOf(
                                SapBatchNumber(
                                    batchNumber = "1",
                                    quantity = 50.0
                                )
                            )
                        )
                    ),
                    comments = "Test"
                )

                val purchaseDeliveryNote = it.manager.purchaseDeliveryNotes.create(purchaseDeliveryNotePayload)

                assertNotNull(purchaseDeliveryNote)
                assertEquals(purchaseDeliveryNotePayload.docDate, purchaseDeliveryNote.docDate)
                assertEquals(purchaseDeliveryNotePayload.businessPartnerCode, purchaseDeliveryNote.businessPartnerCode)
                assertEquals(purchaseDeliveryNotePayload.salesPersonCode, purchaseDeliveryNote.salesPersonCode)
                assertEquals(purchaseDeliveryNotePayload.lines.size, purchaseDeliveryNote.lines.size)
                assertEquals(purchaseDeliveryNotePayload.lines[0].itemCode, purchaseDeliveryNote.lines[0].itemCode)
                assertEquals(purchaseDeliveryNotePayload.lines[0].quantity, purchaseDeliveryNote.lines[0].quantity)
                assertEquals(purchaseDeliveryNotePayload.lines[0].unitPrice, purchaseDeliveryNote.lines[0].unitPrice)
                assertEquals(purchaseDeliveryNotePayload.lines[0].warehouseCode, purchaseDeliveryNote.lines[0].warehouseCode)
                assertEquals(purchaseDeliveryNotePayload.lines[0].batchNumbers.size, purchaseDeliveryNote.lines[0].batchNumbers.size)
                assertEquals(purchaseDeliveryNotePayload.lines[0].batchNumbers[0].batchNumber, purchaseDeliveryNote.lines[0].batchNumbers[0].batchNumber)
                assertEquals(purchaseDeliveryNotePayload.lines[0].batchNumbers[0].quantity, purchaseDeliveryNote.lines[0].batchNumbers[0].quantity)
                assertEquals(purchaseDeliveryNotePayload.comments, purchaseDeliveryNote.comments)

                it.nullAccess.purchaseDeliveryNotes.assertCreateFail(401, purchaseDeliveryNotePayload)
                it.invalidAccess.purchaseDeliveryNotes.assertCreateFail(401, purchaseDeliveryNotePayload)
                it.user.purchaseDeliveryNotes.assertCreateFail(403, purchaseDeliveryNotePayload)
            }
        }
    }

}