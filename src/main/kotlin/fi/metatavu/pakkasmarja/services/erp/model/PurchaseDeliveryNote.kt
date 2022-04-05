package fi.metatavu.pakkasmarja.services.erp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
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
data class PurchaseDeliveryNote (
    val docObjectCode: String = "oPurchaseDeliveryNotes",
    val docDate: String,
    val cardCode: String,
    val comments: String?,
    val salesPersonCode: Int,
    val documentLines: List<PurchaseDeliveryNoteLine>
)