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
    lateinit var sapSessionController: SapSessionController

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var configController: ConfigController

    @Inject
    lateinit var itemsController: ItemsController

    /**
     * Lists contracts
     *
     * @param startDate start date filter
     * @param businessPartnerCode business partner code filter
     * @param contractStatus contract status filter
     * @return list of contracts
     */
    fun listContracts(startDate: LocalDate?, businessPartnerCode: String?, contractStatus: SapContractStatus?): List<JsonNode> {
        sapSessionController.createSapSession().use { sapSession ->  
            val resourceUrl = "${sapSession.apiUrl}/BlanketAgreements"
            val combinedFilter = getCombinedFilter(
                startDate = startDate,
                businessPartnerCode = businessPartnerCode,
                contractStatus = contractStatus
            )

            val items = itemsController.listItems(
                itemGroupCode = null,
                updatedAfter = null,
                firstResult = null,
                maxResults = null
            )

            return if (combinedFilter.isEmpty()) {
                val contracts = getDataFromSap(
                    resourceUrl = resourceUrl,
                    select = "\$select=*",
                    routeId = sapSession.routeId,
                    sessionId = sapSession.sessionId
                )

                spreadContracts(contracts = contracts, items = items)
            } else {
                val contracts = getDataFromSap(
                    resourceUrl = resourceUrl,
                    select = "\$select=*",
                    filter = "\$filter=$combinedFilter",
                    routeId = sapSession.routeId,
                    sessionId = sapSession.sessionId
                )

                spreadContracts(contracts = contracts, items = items)
            }
        }
    }

    /**
     * Constructs combined filter string for SAP request
     *
     * @param startDate start date or null
     * @param businessPartnerCode business partner code or null
     * @param contractStatus contract status or null
     * @returns constructed filter string
     */
    private fun getCombinedFilter(startDate: LocalDate?, businessPartnerCode: String?, contractStatus: SapContractStatus?): String {
        val startDateFilter = startDate?.let { "StartDate ge '$startDate'" }
        val businessPartnerCodeFilter = businessPartnerCode?.let { "BPCode eq '$businessPartnerCode'" }
        val contractStatusFilter = contractStatus?.let { "Status eq '${contractStatusToSapFormat(contractStatus)}'" }
        return listOfNotNull(startDateFilter, businessPartnerCodeFilter, contractStatusFilter).joinToString(" and ")
    }

    /**
     * Spreads contracts to one contract per item group
     *
     * @param contracts contracts to spread
     * @param items list of SAP items
     * @return spread
     */
    private fun spreadContracts(contracts: List<JsonNode>, items: List<JsonNode>): List<JsonNode> {
        val newContracts = mutableListOf<JsonNode>()

        contracts.forEach { contract ->
            try {
                val newContractsForThisContract = spreadContract(contract = contract, items = items)
                newContracts.addAll(newContractsForThisContract)
            } catch (e: Exception) {
                logger.error("Failed to compress a contract from SAP: ${e.message}")
            }
        }

        return newContracts
    }

    /**
     * Spreads a contract to one contract per item group
     *
     * @param contract a contract to spread
     * @param items items to use for spreading
     * @return spread contract
     */
    private fun spreadContract(contract: JsonNode, items: List<JsonNode>): List<JsonNode> {
        val newContractsForThisContract = mutableListOf<JsonNode>()
        val itemLines = contract.get("BlanketAgreements_ItemsLines")
        val groupCodes = configController.getGroupCodesFile()

        itemLines.forEach { itemLine ->
            val itemCode = itemLine.get("ItemNo").asText()
            val item = itemsController.findItemFromItemList(items = items, itemCode = itemCode)

            if (item != null) {
                val itemGroupCode = itemsController.getItemGroupCode(item = item, groupCodes = groupCodes)

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
        }

        return newContractsForThisContract
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

            val contracts = getDataFromSap(
                resourceUrl = resourceUrl,
                select = "\$select=*",
                filter = filter,
                routeId = sapSession.routeId,
                sessionId = sapSession.sessionId
            )

            val items = itemsController.listItems(
                itemGroupCode = null,
                updatedAfter = null,
                firstResult = null,
                maxResults = null
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

                return spreadContract(contract = createdItem, items = items)[0]
            } else if(contracts.size == 1) {
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

                return spreadContract(contract = updatedItem, items = items).find { contract ->
                    contract.get("ItemGroupCode").asInt() == sapContract.itemGroupCode
                }!!
            } else {
                throw Exception("More then one contract was found")
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
        val itemGroupPropertyName = groupCodeObject.get("itemGroupPropertyName").asText()
        val isFrozen = groupCodeObject.get("isFrozen").asBoolean()
        val isOrganic = groupCodeObject.get("isOrganic").asBoolean()

        val isFrozenFilter = "Properties28 eq ${toSapItemPropertyBoolean(isFrozen)}"
        val isOrganicFilter = "Properties35 eq ${toSapItemPropertyBoolean(isOrganic)}"

        val filter = "\$filter=$itemGroupPropertyName eq tYES and $isFrozenFilter and $isOrganicFilter"
        val select = "\$select=ItemCode"
        val items = getDataFromSap(
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
            true -> "tYES"
            false -> "tNO"
        }
    }
}