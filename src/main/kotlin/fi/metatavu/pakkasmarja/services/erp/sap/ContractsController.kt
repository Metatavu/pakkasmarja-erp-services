package fi.metatavu.pakkasmarja.services.erp.sap

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import fi.metatavu.pakkasmarja.services.erp.api.model.SapContract
import fi.metatavu.pakkasmarja.services.erp.api.model.SapContractStatus
import fi.metatavu.pakkasmarja.services.erp.config.ConfigController
import fi.metatavu.pakkasmarja.services.erp.sap.session.SapSession
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

    @Inject
    private lateinit var configController: ConfigController

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
                val contracts = getItemsAsJsonNodes(
                    resourceUrl = resourceUrl,
                    select = "\$select=*",
                    routeId = sapSession.routeId,
                    sessionId = sapSession.sessionId
                )
                return compressContracts(contracts = contracts, session = sapSession)
            }

            val filter = "\$filter=$combinedFilter"

            val contracts = getItemsAsJsonNodes(
                resourceUrl = resourceUrl,
                select = "\$select=*",
                filter = filter,
                routeId = sapSession.routeId,
                sessionId = sapSession.sessionId
            )
            return compressContracts(contracts = contracts, session = sapSession)
        }
    }

    /**
     * Compresses contracts. From one contract per item to one contract per item group.
     *
     * @param contracts contracts to compress
     * @param session SAP session to use
     * @return compressed contracts
     */
    private fun compressContracts(contracts: List<JsonNode>, session: SapSession): List<JsonNode> {
        val newContracts = mutableListOf<JsonNode>()
        val groupCodes = configController.getGroupCodesFile()
        val itemPropertiesSelect = getItemPropertiesSelect(groupCodes = groupCodes)
        val items = getItemsAsJsonNodes(
            resourceUrl = "${session.apiUrl}/Items",
            select = itemPropertiesSelect,
            sessionId = session.sessionId,
            routeId = session.routeId
        )

        contracts.forEach { contract ->
            try {
                val newContractsForThisContract = mutableListOf<JsonNode>()
                val itemLines = contract.get("BlanketAgreements_ItemsLines")
                itemLines.forEach { itemLine ->
                    val itemCode = itemLine.get("ItemNo").asText()
                    val itemGroupCode = getItemGroupCode(itemCode = itemCode, items = items, groupCodes = groupCodes)

                    if (itemGroupCode != null) {
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
                }

                newContracts.addAll(newContractsForThisContract)
            } catch (e: Exception) {
                logger.error("Failed to compress a contract from SAP: ${e.message}")
            }
        }

        return newContracts
    }

    /**
     * Creates a SAP query selector from group codes config
     *
     * @param groupCodes groups to use for selection
     * @return selector
     */
    private fun getItemPropertiesSelect(groupCodes: JsonNode): String {
        val propertyNames = mutableListOf<String>()
        groupCodes.forEach { groupCode ->
            val itemPropertyName = groupCode.get("itemPropertyName").asText()
            propertyNames.add(itemPropertyName)
        }

        val filters = propertyNames.joinToString(",").plus(",ItemCode,Properties28,Properties35")
        return "\$select=$filters"
    }

    /**
     * Gets the group code of an item
     *
     * @param itemCode item code
     * @param items items
     * @param groupCodes group codes config
     * @return group code
     */
    private fun getItemGroupCode(itemCode: String, items: List<JsonNode>, groupCodes: JsonNode): Int? {
        val item = items.find { item -> item.get("ItemCode").asText() == itemCode }

        if (item == null) {
            logger.error("Item not found with code $itemCode")
            return null
        }

        val itemIsFrozen = item.get("Properties28").asText() == "tYes"
        val itemIsOrganic = item.get("Properties35").asText() == "tYes"

        groupCodes.fields().forEach { pair ->
            val groupCode = pair.value
            val itemPropertyName = groupCode.get("itemPropertyName").asText()
            val itemIsOfGroup = item.get(itemPropertyName).asText() == "tYes"
            val groupIsFrozen = groupCode.get("isFrozen").asBoolean()
            val groupIsOrganic = groupCode.get("isOrganic").asBoolean()
            if (itemIsOfGroup && groupIsFrozen == itemIsFrozen && groupIsOrganic == itemIsOrganic) {
                return pair.key.toInt()
            }
        }

        return null
    }

    /**
     * Translates a contract status to the format used by SAP
     *
     * @param contractStatus a contract status to translate
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