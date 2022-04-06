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
class StockTransfer() {

    @JsonProperty("DocDate")
    private var docDate: String? = null

    @JsonProperty("CardCode")
    private var cardCode: String? = null

    @JsonProperty("Comments")
    private var comments: String? = null

    @JsonProperty("SalesPersonCode")
    private var salesPersonCode: Int? = null

    @JsonProperty("FromWarehouse")
    private var fromWarehouse: String? = null

    @JsonProperty("ToWarehouse")
    private var toWarehouse: String? = null

    @JsonProperty("StockTransferLines")
    private var stockTransferLines: List<StockTransferLine> = listOf()

    constructor(
        docDate: String?,
        cardCode: String?,
        comments: String?,
        salesPersonCode: Int?,
        fromWarehouse: String?,
        toWarehouse: String?,
        stockTransferLines: List<StockTransferLine>
    ) : this() {
        this.docDate = docDate
        this.cardCode = cardCode
        this.comments = comments
        this.salesPersonCode = salesPersonCode
        this.fromWarehouse = fromWarehouse
        this.toWarehouse = toWarehouse
        this.stockTransferLines = stockTransferLines
    }

    fun getDocDate(): String? {
        return docDate
    }

    fun setDocDate(docDate: String?) {
        this.docDate = docDate
    }

    fun getCardCode(): String? {
        return cardCode
    }

    fun setCardCode(cardCode: String?) {
        this.cardCode = cardCode
    }

    fun getComments(): String? {
        return comments
    }

    fun setComments(comments: String?) {
        this.comments = comments
    }

    fun getSalesPersonCode(): Int? {
        return salesPersonCode
    }

    fun setSalesPersonCode(salesPersonCode: Int?) {
        this.salesPersonCode = salesPersonCode
    }

    fun getFromWarehouse(): String? {
        return fromWarehouse
    }

    fun setFromWarehouse(fromWarehouse: String?) {
        this.fromWarehouse = fromWarehouse
    }

    fun getToWarehouse(): String? {
        return toWarehouse
    }

    fun setToWarehouse(toWarehouse: String?) {
        this.toWarehouse = toWarehouse
    }

    fun getStockTransferLines(): List<StockTransferLine> {
        return stockTransferLines
    }

    fun setStockTransferLines(stockTransferLines: List<StockTransferLine>) {
        this.stockTransferLines = stockTransferLines
    }

}