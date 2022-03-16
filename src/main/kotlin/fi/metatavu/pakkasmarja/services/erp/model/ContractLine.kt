package fi.metatavu.pakkasmarja.services.erp.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Data class ContractLine
 *
 * @author Jari Nykänen
 */
data class ContractLine(
    @JsonProperty("ItemNo")
    val itemNo: String,
    @JsonProperty("PlannedQuantity")
    val plannedQuantity: Double,
    @JsonProperty("CumulativeQuantity")
    val cumulativeQuantity: Double,
    @JsonProperty("ShippingType")
    val shippingType: Int
)