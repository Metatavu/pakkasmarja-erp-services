package fi.metatavu.pakkasmarja.services.erp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * Model class ContractLine
 *
 * @author Jari Nykänen
 * @author Antti Leppä
 */
@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
class ContractLine() {

    @JsonProperty("ItemNo")
    private var itemNo: String? = null

    @JsonProperty("PlannedQuantity")
    private var plannedQuantity: Double? = null

    @JsonProperty("CumulativeQuantity")
    private var cumulativeQuantity: Double? = null

    @JsonProperty("ShippingType")
    private var shippingType: Int? = null

    constructor(itemNo: String?, plannedQuantity: Double?, cumulativeQuantity: Double?, shippingType: Int?) : this() {
        this.itemNo = itemNo
        this.plannedQuantity = plannedQuantity
        this.cumulativeQuantity = cumulativeQuantity
        this.shippingType = shippingType
    }

    fun getItemNo(): String? {
        return itemNo
    }

    fun setItemNo(itemNo: String?) {
        this.itemNo = itemNo
    }

    fun getPlannedQuantity(): Double? {
        return plannedQuantity
    }

    fun setPlannedQuantity(plannedQuantity: Double?) {
        this.plannedQuantity = plannedQuantity
    }

    fun getCumulativeQuantity(): Double? {
        return cumulativeQuantity
    }

    fun setCumulativeQuantity(cumulativeQuantity: Double?) {
        this.cumulativeQuantity = cumulativeQuantity
    }

    fun getShippingType(): Int? {
        return shippingType
    }

    fun setShippingType(shippingType: Int?) {
        this.shippingType = shippingType
    }

}