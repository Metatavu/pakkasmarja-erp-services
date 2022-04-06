package fi.metatavu.pakkasmarja.services.erp.model

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
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

    private var additionalFields: MutableMap<String, Any> = mutableMapOf()

    constructor(
        itemNo: String?,
        plannedQuantity: Double?,
        cumulativeQuantity: Double?,
        shippingType: Int?,
        additionalFields: MutableMap<String, Any>
    ) : this() {
        this.itemNo = itemNo
        this.plannedQuantity = plannedQuantity
        this.cumulativeQuantity = cumulativeQuantity
        this.shippingType = shippingType
        this.additionalFields = additionalFields
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

    @JsonAnyGetter
    fun getAdditionalFields(): MutableMap<String, Any> {
        return additionalFields
    }

    @JsonAnySetter
    fun setAdditionalFields(name: String, value: Any) {
        this.additionalFields[name] = value
    }
}