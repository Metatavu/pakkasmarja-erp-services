package fi.metatavu.pakkasmarja.services.erp.sap

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import fi.metatavu.pakkasmarja.services.erp.api.model.SapContractStatus
import fi.metatavu.pakkasmarja.services.erp.sap.session.SapSessionController
import org.jboss.logging.Logger
import java.time.LocalDate
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * The controller for contracts
 */
@ApplicationScoped
class ContractsController: AbstractSapResourceController() {
    @Inject
    private lateinit var sapSessionController: SapSessionController

    @Inject
    private lateinit var logger: Logger

    /**
     * Lists contracts
     *
     * @param startDate start date filter
     * @param businessPartnerCode business partner code filter
     * @param contractStatus contract status filter
     * @return contracts
     */
    fun listContracts(startDate: LocalDate?, businessPartnerCode: String?, contractStatus: SapContractStatus?): List<JsonNode> {
        sapSessionController.createSapSession().use { sapSession ->  
            val resourceUrl = "${sapSession.apiUrl}/BlanketAgreements"
            val startDateFilter = startDate?.let { "StartDate ge '$startDate'" }
            val businessPartnerCodeFilter = businessPartnerCode?.let { "BPCode eq '$businessPartnerCode'" }
            val contractStatusFilter = contractStatus?.let { "Status eq '${contractStatusToSapFormat(contractStatus)}'" }
            val combinedFilter = listOfNotNull(startDateFilter, businessPartnerCodeFilter, contractStatusFilter).joinToString(" and ")

            if (combinedFilter.isEmpty()) {
                return compressContracts(getItemsAsJsonNodes(
                    resourceUrl = resourceUrl,
                    select = "\$select=*",
                    routeId = sapSession.routeId,
                    sessionId = sapSession.sessionId
                ))
            }

            val filter = "\$filter=$combinedFilter"

            return compressContracts(getItemsAsJsonNodes(
                resourceUrl = resourceUrl,
                filter = filter,
                select = "\$select=*",
                routeId = sapSession.routeId,
                sessionId = sapSession.sessionId
            ))
        }
    }

    /**
     * Compresses contracts. From one contract per item to one contract per item group.
     *
     * @param contracts contracts to compress
     * @return compressed contracts
     */
    private fun compressContracts(contracts: List<JsonNode>): List<JsonNode> {
        val newContracts = mutableListOf<JsonNode>()

        contracts.forEach { contract ->
            try {
                val newContractsForThisContract = mutableListOf<JsonNode>()
                val itemLines = contract.get("BlanketAgreements_ItemsLines")
                itemLines.forEach { itemLine ->
                    val itemGroupCode = itemLine.get("ItemGroup").asText().toInt()
                    val foundContract = newContractsForThisContract.find { contractToCheck -> contractToCheck.get("ItemGroupCode").asInt() == itemGroupCode }
                    if (foundContract == null) {
                        val newContract = contract as ObjectNode
                        newContract.put("ItemGroupCode", itemGroupCode)
                        newContract.put("DeliveredQuantity", 1)
                        newContractsForThisContract.add(newContract as JsonNode)
                    } else {
                        val currentCount = foundContract.get("DeliveredQuantity").asInt()
                        val indexOfContract = newContractsForThisContract.indexOf(foundContract)
                        val newContract = foundContract as ObjectNode
                        newContract.put("DeliveredQuantity", currentCount + 1)
                        newContractsForThisContract[indexOfContract] = newContract
                    }
                }

                newContracts.addAll(newContractsForThisContract)
            } catch (e: Exception) {
                logger.error("Failed to compress a contract from SAP: ${e.message}")
            }
        }

        return newContracts
    }

    /**
     * Translates a contract status to the format used by SAP
     *
     * @return a contract status in the format used by SAP
     */
    private fun contractStatusToSapFormat(contractStatus: SapContractStatus): String {
        return when(contractStatus) {
            SapContractStatus.APPROVED -> "asApproved"
            SapContractStatus.DRAFT -> "asDraft"
            SapContractStatus.ON_HOLD -> "asOnHold"
            SapContractStatus.TERMINATED -> "asTerminated"
        }
    }
}