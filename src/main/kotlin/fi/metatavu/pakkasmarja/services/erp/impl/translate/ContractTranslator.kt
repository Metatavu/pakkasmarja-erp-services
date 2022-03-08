package fi.metatavu.pakkasmarja.services.erp.impl.translate

import com.fasterxml.jackson.databind.JsonNode
import fi.metatavu.pakkasmarja.services.erp.api.model.SapContract
import fi.metatavu.pakkasmarja.services.erp.api.model.SapContractStatus
import javax.enterprise.context.ApplicationScoped

/**
 * The translator class for SAP contracts
 */
@ApplicationScoped
class ContractTranslator: AbstractTranslator<JsonNode, SapContract>() {

    /**
     * Translates a contract from SAP into tbe format expected by spec
     *
     * @param node a contract from SAP
     * @return translated contract
     */
    override fun translate(node: JsonNode): SapContract {
        val startDate = resolveLocalDate(node.get("StartDate").asText())
        val year = startDate?.year.toString()

        return SapContract(
            id = "$year-${node.get("DocNum").asText()}",
            businessPartnerCode = node.get("BPCode").asText().toInt(),
            contactPersonCode = node.get("ContractPersonCode").asText().toInt(),
            itemGroupCode = node.get("ItemGroupCode").asInt(),
            status = resolveContractStatus(node.get("Status").asText())!!,
            deliveredQuantity = node.get("DeliveredQuantity").asDouble(),
            startDate = startDate,
            endDate = resolveLocalDate(node.get("EndDate").asText()),
            signingDate = resolveLocalDate(node.get("SigningDate").asText()),
            terminateDate = resolveLocalDate(node.get("TerminateDate").asText()),
            remarks = node.get("Remarks").asText()
        )
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