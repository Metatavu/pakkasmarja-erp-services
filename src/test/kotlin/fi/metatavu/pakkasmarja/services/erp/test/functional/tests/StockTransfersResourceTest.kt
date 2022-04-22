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
 * Tests for stock transfers
 *
 * TODO: Add support for invalid data testing
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(SapMockTestResource::class)
)
@TestProfile(LocalTestProfile::class)
class StockTransfersResourceTest: AbstractResourceTest() {

    /**
     * Tests creating a stock transfer
     */
    @Test
    fun testCreateStockTransfer() {
        createTestBuilder().use {
            SapMock().use { sapMock ->
                sapMock.mockItems("1")
                sapMock.mockBusinessPartners("1")

                val stockTransferPayload = SapStockTransfer(
                    docDate = "2022-04-01",
                    businessPartnerCode = 1,
                    salesPersonCode = 123,
                    fromWarehouse = "100",
                    toWarehouse = "100",
                    lines = arrayOf(
                        SapStockTransferLine(
                            itemCode = 1,
                            quantity = 50.0,
                            binAllocations = arrayOf(
                                SapStockTransferLineBinAllocation(
                                    absEntry = 2,
                                    actionType = BinActionType.TO_WAREHOUSE
                                ),
                                SapStockTransferLineBinAllocation(
                                    absEntry = 3,
                                    actionType = BinActionType.FROM_WAREHOUSE
                                )
                            )
                        )
                    ),
                    comments = "Test"
                )

                val stockTransfer = it.manager.stockTransfers.create(stockTransferPayload)

                assertNotNull(stockTransfer)
                assertEquals(stockTransferPayload.docDate, stockTransfer.docDate)
                assertEquals(stockTransferPayload.businessPartnerCode, stockTransfer.businessPartnerCode)
                assertEquals(stockTransferPayload.salesPersonCode, stockTransfer.salesPersonCode)
                assertEquals(stockTransferPayload.fromWarehouse, stockTransfer.fromWarehouse)
                assertEquals(stockTransferPayload.toWarehouse, stockTransfer.toWarehouse)
                assertEquals(stockTransferPayload.lines.size, stockTransfer.lines.size)
                assertEquals(stockTransferPayload.lines[0].itemCode, stockTransfer.lines[0].itemCode)
                assertEquals(stockTransferPayload.lines[0].quantity, stockTransfer.lines[0].quantity)
                assertEquals(stockTransferPayload.lines[0].binAllocations.size, stockTransfer.lines[0].binAllocations.size)
                assertEquals(stockTransferPayload.lines[0].binAllocations[0].absEntry, stockTransfer.lines[0].binAllocations[0].absEntry)
                assertEquals(stockTransferPayload.lines[0].binAllocations[0].actionType, stockTransfer.lines[0].binAllocations[0].actionType)
                assertEquals(stockTransferPayload.lines[0].binAllocations[1].absEntry, stockTransfer.lines[0].binAllocations[1].absEntry)
                assertEquals(stockTransferPayload.lines[0].binAllocations[1].actionType, stockTransfer.lines[0].binAllocations[1].actionType)
                assertEquals(stockTransferPayload.comments, stockTransfer.comments)

                it.nullAccess.stockTransfers.assertCreateFail(401, stockTransferPayload)
                it.invalidAccess.stockTransfers.assertCreateFail(401, stockTransferPayload)
                it.user.stockTransfers.assertCreateFail(403, stockTransferPayload)
            }
        }
    }

}