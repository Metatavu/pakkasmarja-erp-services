package fi.metatavu.pakkasmarja.services.erp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * Data class for SAPItemGroupContract
 *
 * @author Jari Nykänen
 */
@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
data class SAPItemGroupContract(
    val startDate: String?,
    val endDate: String?,
    val docNum: Int,
    val bPCode: String,
    val contactPersonCode: Int?,
    val status: String?,
    val signingDate: String?,
    val terminateDate: String?,
    val remarks: String?,
    val agreementNo: Int,
    val cumulativeQuantity: Double?,
    val itemGroupCode: Int?
)