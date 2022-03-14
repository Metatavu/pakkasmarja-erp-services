package fi.metatavu.pakkasmarja.services.erp.sap

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.metatavu.pakkasmarja.services.erp.api.model.SapContract
import fi.metatavu.pakkasmarja.services.erp.api.model.SapContractStatus
import fi.metatavu.pakkasmarja.services.erp.config.ConfigController
import fi.metatavu.pakkasmarja.services.erp.model.*
import fi.metatavu.pakkasmarja.services.erp.sap.session.SapSession
import fi.metatavu.pakkasmarja.services.erp.sap.session.SapSessionController
import org.jboss.logging.Logger
import java.time.LocalDate
import javax.enterprise.context.RequestScoped
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
    fun listContracts(startDate: LocalDate?, businessPartnerCode: String?, contractStatus: SapContractStatus?): List<SAPItemGroupContract> {
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

            val contracts = if (combinedFilter.isEmpty()) {
                getContracts(resourceUrl = resourceUrl, sapSession = sapSession, filter = null)
            } else {
                getContracts(resourceUrl = resourceUrl, sapSession = sapSession, filter = "\$filter=$combinedFilter")
            }

            return spreadContracts(contracts = contracts, items = items)
        }
    }

    /**
     * Creates a new contract
     *
     * @param sapContract a contract to create
     * @return created contract
     */
    fun createContract(sapContract: SapContract): SAPItemGroupContract? {
        return try {
            sapSessionController.createSapSession().use { sapSession ->
                val resourceUrl = "${sapSession.apiUrl}/BlanketAgreements"
                val filter = "\$filter=StartDate ge ${sapContract.startDate} and BPCode eq ${sapContract.businessPartnerCode} and Status eq asApproved"

                val contracts = getContracts(
                    resourceUrl = resourceUrl,
                    sapSession = sapSession,
                    filter = filter
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

                    val createdContract = createSapEntity(
                        targetClass = Contract::class.java,
                        item = jacksonObjectMapper().writeValueAsString(newContract),
                        resourceUrl = resourceUrl,
                        sessionId = sapSession.sessionId,
                        routeId = sapSession.routeId
                    ) ?: return null

                    return spreadContract(contract = createdContract, items = items)[0]
                } else {
                    val contractToUpdate = contracts.first()
                    val contractForUpdate = buildContractForUpdate(
                        contractToUpdate = contractToUpdate,
                        newData = sapContract,
                        sapSession = sapSession
                    )

                    val updatedItem = updateSapEntity(
                        targetClass = Contract::class.java,
                        item = jacksonObjectMapper().writeValueAsString(contractForUpdate),
                        resourceUrl = "$resourceUrl%28${contractToUpdate.docNum}%29",
                        sessionId = sapSession.sessionId,
                        routeId = sapSession.routeId
                    ) ?: return null

                    return spreadContract(contract = updatedItem, items = items)[0]
                }
            }
        } catch (error: Exception) {
            logger.error("Error while creating SAP contract: ${error.message}")
            null
        }
    }

    /**
     * Gets list of contracts
     *
     * @param resourceUrl resource URL
     * @param sapSession current active SAP session
     * @param filter filter string or null
     * @return list of contracts
     */
    private fun getContracts(resourceUrl: String, sapSession: SapSession, filter: String?): List<Contract> {
        var filterString: String? = null

        if (filter != null) {
            filterString = filter
        }

        val requestUrl = constructSAPRequestUrl(
            baseUrl = resourceUrl,
            select = "\$select=*",
            filter = filterString,
            firstResult = null
        )

        return sapListContractsRequest(
            requestUrl = requestUrl,
            sapSession = sapSession,
            maxResults = null
        ) ?: return emptyList()
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
    private fun spreadContracts(contracts: List<Contract>, items: List<Item>): List<SAPItemGroupContract> {
        val newContracts = mutableListOf<SAPItemGroupContract>()

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
    private fun spreadContract(contract: Contract, items: List<Item>): List<SAPItemGroupContract> {
        val itemGroupDeliveredQuantities = getItemGroupsFromContract(contract, items)

        return itemGroupDeliveredQuantities.map { item ->
            SAPItemGroupContract(
                startDate = contract.startDate,
                endDate = contract.endDate,
                docNum = contract.docNum,
                bPCode = contract.bPCode,
                contactPersonCode = contract.contactPersonCode,
                status = contract.status,
                signingDate = contract.signingDate,
                terminateDate = contract.terminateDate,
                remarks = contract.remarks,
                agreementNo = contract.agreementNo ?: 0,
                cumulativeQuantity = item.value,
                itemGroupCode = item.key
            )
        }
    }

    /**
     * Gets item groups from contract
     *
     * @param contract contract
     * @param items list of items
     * @return map of group code as key and cumulative quantity as value
     */
    private fun getItemGroupsFromContract(contract: Contract, items: List<Item>): Map<Int, Double> {
        val itemGroupsInContract = mutableMapOf<Int, Double>()
        val itemLines = contract.contractLines
        val groupProperties = configController.getGroupPropertiesFromConfigFile()

        itemLines.forEach { itemLine ->
            val item = itemsController.findItemFromItemList(items = items, itemCode = itemLine.itemNo)

            if (item != null) {
                val itemGroupCode = itemsController.getItemGroupCode(item = item, groupProperties = groupProperties)

                if (itemGroupCode != null) {
                    if (!itemGroupsInContract.containsKey(itemGroupCode)) {
                        itemGroupsInContract[itemGroupCode] = itemLine.cumulativeQuantity
                    } else {
                        itemGroupsInContract[itemGroupCode]?.plus(itemLine.cumulativeQuantity)
                    }
                }
            }
        }

        return itemGroupsInContract
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
     * Builds a contract for update
     *
     * @param contractToUpdate a contract to update
     * @param newData new data
     * @param sapSession a SAP session to use
     * @return a contract for update
     */
    private fun buildContractForUpdate(contractToUpdate: Contract, newData: SapContract, sapSession: SapSession): Contract {
        val itemCodesToBeAdded = getGroupItemCodes(groupCode = newData.itemGroupCode, sapSession = sapSession)
        val existingContractLines = contractToUpdate.contractLines
        val itemCodesToAdd = mutableListOf<String>()

        itemCodesToBeAdded.map { itemCode ->
            if (existingContractLines.find { existingContract -> existingContract.itemNo == itemCode } == null) {
                itemCodesToAdd.add(itemCode)
            }
        }

        val newLines = itemCodesToAdd.map { itemCode ->
            ContractLine(
                itemNo = itemCode,
                plannedQuantity = 1.0,
                cumulativeQuantity = 0.0,
                shippingType = -1
            )
        }

        val allLines = mutableListOf<ContractLine>()
        allLines.addAll(existingContractLines)
        allLines.addAll(newLines)

        return contractToUpdate.copy(contractLines = allLines)
    }

    /**
     * Builds a new contract for SAP
     *
     * @param sapContract a SAP contract to build
     * @param sapSession a SAP session to use
     * @return built SAP contract
     */
    private fun buildNewSapContract(sapContract: SapContract, sapSession: SapSession): Contract {
        val itemCodes = getGroupItemCodes(
            groupCode = sapContract.itemGroupCode,
            sapSession = sapSession
        )

        val lines = itemCodes.map { code ->
            ContractLine(
                itemNo = code,
                plannedQuantity = 1.0,
                cumulativeQuantity = 0.0,
                shippingType = -1
            )
        }

        return Contract(
            docNum = sapContract.id.split("-")[1].toInt(),
            startDate = sapContract.startDate.toString(),
            endDate = sapContract.endDate.toString(),
            terminateDate = sapContract.terminateDate.toString(),
            status = contractStatusToSapFormat(SapContractStatus.APPROVED),
            bPCode = sapContract.businessPartnerCode.toString(),
            contactPersonCode = sapContract.contactPersonCode,
            remarks = sapContract.remarks ?: "",
            signingDate = sapContract.signingDate.toString(),
            contractLines = lines
        )
    }

    /**
     * Returns a list of item codes belonging to a single group
     *
     * @param groupCode group code
     * @param sapSession a SAP session to use
     * @return item codes
     */
    private fun getGroupItemCodes(groupCode: Int, sapSession: SapSession): List<String> {
        val filter = constructGroupCodeFilter(groupCode)
        val select = "\$select=ItemCode"

        val requestUrl = constructSAPRequestUrl(
            baseUrl = "${sapSession.apiUrl}/Items",
            select = select,
            filter = filter,
            firstResult = null
        )

        val itemsResponse = sapListItemsRequest(
            requestUrl = requestUrl,
            sapSession = sapSession,
            maxResults = null
        ) ?: return emptyList()

        val items: List<Item> = itemsResponse
        return items.map{ item -> item.itemCode }
    }

    /**
     * Constructs group code filter for contract listing
     *
     * @param code group code
     * @return constructed filter string
     */
    private fun constructGroupCodeFilter(code: Int): String {
        val groupCode = configController.getGroupPropertiesFromConfigFile().find { groupCode -> groupCode.code == code } ?: return ""
        val isOrganicFilter = "Properties21 eq ${toSapItemPropertyBoolean(groupCode.isOrganic)}"
        val isFrozenFilter = "Properties28 eq ${toSapItemPropertyBoolean(groupCode.isFrozen)}"

        return "\$filter=${groupCode.itemGroupPropertyName} eq tYES and $isOrganicFilter and $isFrozenFilter"
    }

}