package fi.metatavu.pakkasmarja.services.erp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * Data class for SAP purchase delivery note line
 *
 * @author Antti Leppä
 */
@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
class PurchaseDeliveryNoteLine() {

    @JsonProperty("ItemCode")
    private var itemCode: String? = null

    @JsonProperty("Quantity")
    private var quantity: Double? = null

    @JsonProperty("UnitPrice")
    private var unitPrice: Double? = null

    @JsonProperty("WarehouseCode")
    private var warehouseCode: String? = null

    @JsonProperty("BatchNumbers")
    private var batchNumbers: List<BatchNumber> = listOf()

    constructor(
        itemCode: String?,
        quantity: Double?,
        unitPrice: Double?,
        warehouseCode: String?,
        batchNumbers: List<BatchNumber>
    ) : this() {
        this.itemCode = itemCode
        this.quantity = quantity
        this.unitPrice = unitPrice
        this.warehouseCode = warehouseCode
        this.batchNumbers = batchNumbers
    }

    fun getItemCode(): String? {
        return itemCode
    }

    fun setItemCode(itemCode: String) {
        this.itemCode = itemCode
    }

    fun getQuantity(): Double? {
        return quantity
    }

    fun setQuantity(quantity: Double) {
        this.quantity = quantity
    }

    fun getUnitPrice(): Double? {
        return unitPrice
    }

    fun setUnitPrice(unitPrice: Double) {
        this.unitPrice = unitPrice
    }

    fun getWarehouseCode(): String? {
        return warehouseCode
    }

    fun setWarehouseCode(warehouseCode: String) {
        this.warehouseCode = warehouseCode
    }

    fun getBatchNumbers(): List<BatchNumber> {
        return batchNumbers
    }

    fun setBatchNumbers(batchNumbers: List<BatchNumber>) {
        this.batchNumbers = batchNumbers
    }

}
