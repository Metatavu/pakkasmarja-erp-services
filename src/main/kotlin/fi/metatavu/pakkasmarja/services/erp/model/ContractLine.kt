package fi.metatavu.pakkasmarja.services.erp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * Data class ContractLine
 *
 * @author Jari Nykänen
 */
@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
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