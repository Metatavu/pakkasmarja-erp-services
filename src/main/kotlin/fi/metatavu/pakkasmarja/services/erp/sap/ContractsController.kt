package fi.metatavu.pakkasmarja.services.erp.sap

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
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

            val groupCodes = configController.getGroupCodesFile()
            val itemPropertiesSelect = getItemPropertiesSelect(groupCodes = groupCodes)
            val items = getItemsAsJsonNodes(
                resourceUrl = "${sapSession.apiUrl}/Items",
                select = itemPropertiesSelect,
                sessionId = sapSession.sessionId,
                routeId = sapSession.routeId
            )

            if (combinedFilter.isEmpty()) {
                val contracts = getItemsAsJsonNodes(
                    resourceUrl = resourceUrl,
                    select = "\$select=*",
                    routeId = sapSession.routeId,
                    sessionId = sapSession.sessionId
                )

                return spreadContracts(contracts = contracts, items = items, groupCodes = groupCodes)
            }

            val filter = "\$filter=$combinedFilter"

            val contracts = getItemsAsJsonNodes(
                resourceUrl = resourceUrl,
                select = "\$select=*",
                filter = filter,
                routeId = sapSession.routeId,
                sessionId = sapSession.sessionId
            )

            return spreadContracts(contracts = contracts, items = items, groupCodes = groupCodes)
        }
    }

    /**
     * Spreads contracts to one contract per item group
     *
     * @param contracts contracts to spread
     * @param items items to use for spreading
     * @param groupCodes group codes to use for spreading
     * @return spread
     */
    private fun spreadContracts(contracts: List<JsonNode>, items: List<JsonNode>, groupCodes: JsonNode): List<JsonNode> {
        val newContracts = mutableListOf<JsonNode>()

        contracts.forEach { contract ->
            try {

                val newContractsForThisContract = spreadContract(contract = contract, items = items, groupCodes = groupCodes)
                newContracts.addAll(newContractsForThisContract)
            } catch (e: Exception) {
                logger.error("Failed to compress a contract from SAP: ${e.message}")
            }
        }

        return newContracts
    }

    /**
     * Spreads a contract to one contract per item group
     * @param contract a contract to spread
     * @param items items to use for spreading
     * @param groupCodes group codes to use for spreading
     * @return spread contract
     */
    private fun spreadContract(contract: JsonNode, items: List<JsonNode>, groupCodes: JsonNode): List<JsonNode> {
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

        return newContractsForThisContract
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

    /**
     * Creates a new contract
     *
     * @param sapContract a contract to create
     * @return created contract
     */
    fun createContract(sapContract: SapContract): JsonNode {
        sapSessionController.createSapSession().use { sapSession ->
            val resourceUrl = "${sapSession.apiUrl}/BlanketAgreements"
            val filter = "\$filter=StartDate ge ${sapContract.startDate} and BPCode eq ${sapContract.businessPartnerCode} and Status eq asApproved"

            val groupCodes = configController.getGroupCodesFile()

            val itemPropertiesSelect = getItemPropertiesSelect(groupCodes = groupCodes)
            val items = getItemsAsJsonNodes(
                resourceUrl = "${sapSession.apiUrl}/Items",
                select = itemPropertiesSelect,
                sessionId = sapSession.sessionId,
                routeId = sapSession.routeId
            )

            val contracts = getItemsAsJsonNodes(
                resourceUrl = resourceUrl,
                select = "\$select=*",
                filter = filter,
                routeId = sapSession.routeId,
                sessionId = sapSession.sessionId
            )

            if (contracts.isEmpty()) {
                val newContract = buildNewSapContract(
                    sapContract = sapContract,
                    sapSession = sapSession
                )

                val createdItem = createItem(
                    item = newContract,
                    resourceUrl = resourceUrl,
                    sessionId = sapSession.sessionId,
                    routeId = sapSession.routeId
                )

                return spreadContract(contract = createdItem, groupCodes = groupCodes, items = items)[0]
            } else {
                val contractToUpdate = contracts.first() as ObjectNode
                val contractForUpdate = buildContractForUpdate(
                    contractToUpdate = contractToUpdate,
                    newData = sapContract,
                    sapSession = sapSession
                )

                val updatedItem = updateItem(
                    item = contractForUpdate,
                    resourceUrl = "$resourceUrl%28${contractToUpdate.get("DocNum").asText()}%28",
                    sessionId = sapSession.sessionId,
                    routeId = sapSession.routeId
                )

                return spreadContract(
                    contract = updatedItem,
                    groupCodes = groupCodes,
                    items = items
                ).find { contract ->
                    contract.get("ItemGroupCode").asInt() == sapContract.itemGroupCode
                }!!
            }
        }
    }

    /**
     * Builds a contract for update
     *
     * @param contractToUpdate a contract to update
     * @param newData new data
     * @param sapSession a SAP session to use
     * @return a contract for update
     */
    private fun buildContractForUpdate(contractToUpdate: ObjectNode, newData: SapContract, sapSession: SapSession): ObjectNode {
        val resourceUrl = "${sapSession.apiUrl}/BlanketAgreements"
        contractToUpdate.put("Status", "asDraft")
        val updatableContract = updateItem(
            item = contractToUpdate,
            resourceUrl = "$resourceUrl%28${contractToUpdate.get("DocNum").asText()}%28",
            sessionId = sapSession.sessionId,
            routeId = sapSession.routeId
        ) as ObjectNode

        updatableContract.put("Status", "asApproved")

        val itemCodesToBeAdded = getGroupItemCodes(groupCode = newData.itemGroupCode.toString(), sapSession = sapSession)
        val existingItemCodes = updatableContract.get("BlanketAgreements_ItemsLines") as ArrayNode
        val itemCodesToAdd = mutableListOf<String>()

        itemCodesToBeAdded.forEach { itemCode ->
            if (existingItemCodes.find { existingCode -> existingCode.get("ItemNo").asText() == itemCode } == null) {
                itemCodesToAdd.add(itemCode)
            }
        }

        val mapper = ObjectMapper()
        val itemLines = mapper.createArrayNode()
        itemCodesToAdd.forEach { itemCode ->
            val itemLine = mapper.createObjectNode()
            itemLine.put("ItemNo", itemCode)
            itemLines.add(itemLine)
        }

        val newItemCodesList = existingItemCodes.addAll(itemLines)
        updatableContract.set<JsonNode>("BlanketAgreements_ItemsLines", newItemCodesList)

        return updatableContract
    }

    /**
     * Builds a new contract for SAP
     *
     * @param sapContract a SAP contract to build
     * @param sapSession a SAP session to use
     * @return built SAP contract
     */
    private fun buildNewSapContract(sapContract: SapContract, sapSession: SapSession): ObjectNode {
        val mapper = ObjectMapper()
        val newContract = mapper.createObjectNode()
        newContract.put("DocNum", sapContract.id.split("-")[1])
        newContract.put("BPCode", sapContract.businessPartnerCode.toString())
        newContract.put("ContractPersonCode", sapContract.contactPersonCode.toString())
        newContract.put("StartDate", sapContract.startDate.toString())
        newContract.put("EndDate", sapContract.endDate.toString())
        newContract.put("TerminateDate", sapContract.terminateDate.toString())
        newContract.put("SigningDate", sapContract.signingDate.toString())
        newContract.put("Remarks", sapContract.remarks)
        newContract.put("Status", "asApproved")

        val itemCodes = getGroupItemCodes(
            groupCode = sapContract.itemGroupCode.toString(),
            sapSession = sapSession
        )

        val itemLines = mapper.createArrayNode()
        itemCodes.forEach { itemCode ->
            val itemLine = mapper.createObjectNode()
            itemLine.put("ItemNo", itemCode)
            itemLines.add(itemLine)
        }

        newContract.set<JsonNode>("BlanketAgreements_ItemsLines", itemLines)

        return newContract
    }

    /**
     * Returns a list of item codes belonging to a single group
     *
     * @param groupCode group code
     * @param sapSession a SAP session to use
     * @return item codes
     */
    private fun getGroupItemCodes(groupCode: String, sapSession: SapSession): List<String> {
        val groupCodes = configController.getGroupCodesFile()
        val groupCodeObject = groupCodes.get(groupCode)
        val itemPropertyName = groupCodeObject.get("itemPropertyName").asText()
        val isFrozen = groupCodeObject.get("isFrozen").asBoolean()
        val isOrganic = groupCodeObject.get("isOrganic").asBoolean()

        val isFrozenFilter = "Properties28 eq ${toSapItemPropertyBoolean(isFrozen)}"
        val isOrganicFilter = "Properties35 eq ${toSapItemPropertyBoolean(isOrganic)}"

        val filter = "\$filter=$itemPropertyName eq tYes and $isFrozenFilter and $isOrganicFilter"
        val select = "\$select=ItemCode"
        val items = getItemsAsJsonNodes(
            resourceUrl = "${sapSession.apiUrl}/Items",
            filter = filter,
            select = select,
            sessionId = sapSession.sessionId,
            routeId = sapSession.routeId
        )

        return items.map { item -> item.get("ItemCode").asText() }
    }

    /**
     * Translates a boolean value to the format used by SAP
     *
     * @param value a value to translate
     * @return a boolean value in the format used by SAP
     */
    private fun toSapItemPropertyBoolean(value: Boolean): String {
        return when (value) {
            true -> "tYes"
            false -> "tNo"
        }
    }
}