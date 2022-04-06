package fi.metatavu.pakkasmarja.services.erp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * Model class for SAP stock transfer line bin allocation
 *
 * @author Antti Leppä
 */
@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
class StockTransferLinesBinAllocation() {

    @JsonProperty("BinAbsEntry")
    private var binAbsEntry: Int? = null

    @JsonProperty("Quantity")
    private var quantity: Double? = null

    @JsonProperty("BinActionType")
    private var binActionType: BinActionType? = null

    constructor(binAbsEntry: Int?, quantity: Double?, binActionType: BinActionType?) : this() {
        this.binAbsEntry = binAbsEntry
        this.quantity = quantity
        this.binActionType = binActionType
    }

    fun getBinAbsEntry(): Int? {
        return binAbsEntry
    }

    fun setBinAbsEntry(binAbsEntry: Int?) {
        this.binAbsEntry = binAbsEntry
    }

    fun getQuantity(): Double? {
        return quantity
    }

    fun setQuantity(quantity: Double?) {
        this.quantity = quantity
    }

    fun getBinActionType(): BinActionType? {
        return binActionType
    }

    fun setBinActionType(binActionType: BinActionType?) {
        this.binActionType = binActionType
    }

}