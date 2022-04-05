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
data class StockTransferLine(
    @JsonProperty("ItemCode")
    val itemCode: String,
    @JsonProperty("Quantity")
    val quantity: Double,
    @JsonProperty("WarehouseCode")
    val warehouseCode: String,
    @JsonProperty("FromWarehouseCode")
    val fromWarehouseCode: String,
    @JsonProperty("StockTransferLinesBinAllocations")
    val stockTransferLinesBinAllocations: List<StockTransferLinesBinAllocation>,
)
