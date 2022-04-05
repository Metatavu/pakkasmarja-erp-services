package fi.metatavu.pakkasmarja.services.erp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * Data class for SAP stock transfer line bin allocation
 *
 * @author Antti Leppä
 */
@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
data class StockTransferLinesBinAllocation(
    @JsonProperty("BinAbsEntry")
    val binAbsEntry: Int,
    @JsonProperty("Quantity")
    val quantity: Double,
    @JsonProperty("BinActionType")
    val binActionType: BinActionType
)
