package fi.metatavu.pakkasmarja.services.erp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * Data class for SAP stock transfer
 *
 * @author Antti Leppä
 */
@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
data class StockTransfer (
    @JsonProperty("DocDate")
    val docDate: String,
    @JsonProperty("CardCode")
    val cardCode: String,
    @JsonProperty("Comments")
    val comments: String?,
    @JsonProperty("SalesPersonCode")
    val salesPersonCode: Int,
    @JsonProperty("FromWarehouse")
    val fromWarehouse: String,
    @JsonProperty("ToWarehouse")
    val toWarehouse: String,
    @JsonProperty("StockTransferLines")
    val stockTransferLines: List<StockTransferLine>
)