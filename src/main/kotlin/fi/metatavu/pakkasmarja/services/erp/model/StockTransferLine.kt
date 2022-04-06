package fi.metatavu.pakkasmarja.services.erp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * Data class for SAP stock transfer line
 *
 * @author Antti Leppä
 */
@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
class StockTransferLine() {
    @JsonProperty("ItemCode")
    private var itemCode: String? = null

    @JsonProperty("Quantity")
    private var quantity: Double? = null

    @JsonProperty("WarehouseCode")
    private var warehouseCode: String? = null

    @JsonProperty("FromWarehouseCode")
    private var fromWarehouseCode: String? = null

    @JsonProperty("StockTransferLinesBinAllocations")
    private var stockTransferLinesBinAllocations: List<StockTransferLinesBinAllocation> = listOf()

    constructor(
        itemCode: String?,
        quantity: Double?,
        warehouseCode: String?,
        fromWarehouseCode: String?,
        stockTransferLinesBinAllocations: List<StockTransferLinesBinAllocation>
    ) : this() {
        this.itemCode = itemCode
        this.quantity = quantity
        this.warehouseCode = warehouseCode
        this.fromWarehouseCode = fromWarehouseCode
        this.stockTransferLinesBinAllocations = stockTransferLinesBinAllocations
    }

    fun getItemCode(): String? {
        return itemCode
    }

    fun setItemCode(itemCode: String?) {
        this.itemCode = itemCode
    }

    fun getQuantity(): Double? {
        return quantity
    }

    fun setQuantity(quantity: Double?) {
        this.quantity = quantity
    }

    fun getWarehouseCode(): String? {
        return warehouseCode
    }

    fun setWarehouseCode(warehouseCode: String?) {
        this.warehouseCode = warehouseCode
    }

    fun getFromWarehouseCode(): String? {
        return fromWarehouseCode
    }

    fun setFromWarehouseCode(fromWarehouseCode: String?) {
        this.fromWarehouseCode = fromWarehouseCode
    }

    fun getStockTransferLinesBinAllocations(): List<StockTransferLinesBinAllocation> {
        return stockTransferLinesBinAllocations
    }

    fun setStockTransferLinesBinAllocations(stockTransferLinesBinAllocations: List<StockTransferLinesBinAllocation>) {
        this.stockTransferLinesBinAllocations = stockTransferLinesBinAllocations
    }

}
