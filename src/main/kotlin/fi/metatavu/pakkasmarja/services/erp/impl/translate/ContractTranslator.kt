package fi.metatavu.pakkasmarja.services.erp.impl.translate

import com.fasterxml.jackson.databind.JsonNode
import fi.metatavu.pakkasmarja.services.erp.api.model.SapContract
import fi.metatavu.pakkasmarja.services.erp.api.model.SapContractStatus
import org.jboss.logging.Logger
import java.time.LocalDate
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * The translator class for SAP contracts
 */
@ApplicationScoped
class ContractTranslator {
    @Inject
    private lateinit var logger: Logger

    /**
     * Translates a contract from SAP into tbe format expected by spec
     *
     * @param contract a contract from SAP
     * @return translated contract
     */
    fun translate(contract: JsonNode): SapContract? {
        return try {
            SapContract(
                id = ,
                businessPartnerCode = contract.get("BPCode").asText().toInt(),
                contactPersonCode = contract.get("ContractPersonCode").asText().toInt(),
                itemGroupCode = ,
                status = resolveContractStatus(contract.get("Status").asText())!!,
                deliveredQuantity = "",
                startDate = resolveLocalDate(contract.get("StartDate").asText()),
                endDate = resolveLocalDate(contract.get("EndDate").asText()),
                signingDate = resolveLocalDate(contract.get("SigningDate").asText()),
                terminateDate = resolveLocalDate(contract.get("TerminateDate").asText()),
                remarks = contract.get("Remarks").asText()
            )
        } catch (e: Exception) {
            logger.error("Failed to translate a contract from SAP: ${e.message}")
            null;
        }
    }

    /**
     * Tries to parse a string to LocalDate and returns null if fails
     *
     * @param date string to parse
     * @return parsed string or null
     */
    private fun resolveLocalDate (date: String): LocalDate? {
        return try {
            LocalDate.parse(date)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Translates a contract status from SAP into the format expect by spec
     *
     * @param status status from SAP
     * @return translated status
     */
    private fun resolveContractStatus(status: String): SapContractStatus? {
        return when (status) {
            "asApproved" -> SapContractStatus.APPROVED
            "asDraft" -> SapContractStatus.DRAFT
            "asOnHold" -> SapContractStatus.ON_HOLD
            "asTerminated" -> SapContractStatus.TERMINATED
            else -> null
        }
    }
}