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
data class PurchaseDeliveryNoteLine(

    @JsonProperty("ItemCode")
    val itemCode: String,

    @JsonProperty("Quantity")
    val quantity: Double,

    @JsonProperty("UnitPrice")
    val unitPrice: Double,

    @JsonProperty("WarehouseCode")
    val warehouseCode: String,

    @JsonProperty("BatchNumbers")
    val batchNumbers: List<BatchNumber>
)
