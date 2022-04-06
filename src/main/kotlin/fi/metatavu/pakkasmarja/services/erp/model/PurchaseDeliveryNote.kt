package fi.metatavu.pakkasmarja.services.erp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * Data class for SAP purchase delivery note
 *
 * @author Antti Leppä
 */
@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
class PurchaseDeliveryNote() {

    @JsonProperty("DocObjectCode")
    private var docObjectCode: String? = null

    @JsonProperty("DocDate")
    private var docDate: String? = null

    @JsonProperty("CardCode")
    private var cardCode: String? = null

    @JsonProperty("Comments")
    private var comments: String? = null

    @JsonProperty("SalesPersonCode")
    private var salesPersonCode: Int? = null

    @JsonProperty("DocumentLines")
    private var documentLines: List<PurchaseDeliveryNoteLine> = listOf()

    constructor(
        docObjectCode: String?,
        docDate: String?,
        cardCode: String?,
        comments: String?,
        salesPersonCode: Int?,
        documentLines: List<PurchaseDeliveryNoteLine>
    ) : this() {
        this.docObjectCode = docObjectCode
        this.docDate = docDate
        this.cardCode = cardCode
        this.comments = comments
        this.salesPersonCode = salesPersonCode
        this.documentLines = documentLines
    }

    fun getDocObjectCode(): String? {
        return docObjectCode
    }

    fun setDocObjectCode(docObjectCode: String?) {
        this.docObjectCode = docObjectCode
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

    fun getDocumentLines(): List<PurchaseDeliveryNoteLine> {
        return documentLines
    }

    fun setDocumentLines(documentLines: List<PurchaseDeliveryNoteLine>) {
        this.documentLines = documentLines
    }

}