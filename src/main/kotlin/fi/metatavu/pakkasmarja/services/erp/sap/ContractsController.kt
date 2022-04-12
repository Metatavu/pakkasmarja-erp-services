package fi.metatavu.pakkasmarja.services.erp.sap

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.metatavu.pakkasmarja.services.erp.api.model.SapContract
import fi.metatavu.pakkasmarja.services.erp.api.model.SapContractStatus
import fi.metatavu.pakkasmarja.services.erp.config.ConfigController
import fi.metatavu.pakkasmarja.services.erp.model.*
import fi.metatavu.pakkasmarja.services.erp.sap.session.SapSession
import java.time.LocalDate
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * The controller for contracts
 */
@ApplicationScoped
class ContractsController: AbstractSapResourceController<Contract>() {

    @Inject
    lateinit var configController: ConfigController

    @Inject
    lateinit var itemsController: ItemsController

    /**
     * Lists contracts
     *
     * @param sapSession SAP session
     * @param startDate start date filter
     * @param businessPartnerCode business partner code filter
     * @param contractStatus contract status filter
     * @return list of contracts
     */
    fun listContracts(
        sapSession: SapSession,
        startDate: LocalDate?,
        businessPartnerCode: String?,
        contractStatus: SapContractStatus?
    ): List<SAPItemGroupContract> {
        val resourceUrl = "${sapSession.apiUrl}/BlanketAgreements"
        val combinedFilter = getCombinedFilter(
            startDate = startDate,
            businessPartnerCode = businessPartnerCode,
            contractStatus = contractStatus
        )

        val contracts = if (combinedFilter.isEmpty()) {
            getContracts(resourceUrl = resourceUrl, sapSession = sapSession, filter = null)
        } else {
            getContracts(resourceUrl = resourceUrl, sapSession = sapSession, filter = "\$filter=$combinedFilter")
        }

        return spreadContracts (
            sapSession = sapSession,
            contracts = contracts
        )
    }

    /**
     * Creates a new contract
     *
     * @param sapSession SAP session
     * @param sapContract a contract to create
     * @return created contract
     */
    fun createContract(
        sapSession: SapSession,
        sapContract: SapContract
    ): SAPItemGroupContract? {
        return try {
            val resourceUrl = "${sapSession.apiUrl}/BlanketAgreements"
            val filter = "\$filter=StartDate ge ${sapContract.startDate} and BPCode eq '${sapContract.businessPartnerCode}' and Status eq SAPB1.BlanketAgreementStatusEnum'asApproved'"

            val contracts = getContracts(
                resourceUrl = resourceUrl,
                sapSession = sapSession,
                filter = filter
            )

            if (contracts.isEmpty()) {
                val newContract = buildNewSapContract(
                    sapContract = sapContract,
                    sapSession = sapSession
                )

                val mapper = jacksonObjectMapper()
                val payload = mapper.writeValueAsString(newContract)

                val createdContract = createSapEntity(
                    targetClass = Contract::class.java,
                    item = payload,
                    resourceUrl = resourceUrl,
                    sessionId = sapSession.sessionId,
                    routeId = sapSession.routeId
                )

                val createdContracts = spreadContract(
                    sapSession = sapSession,
                    contract = createdContract
                )

                return createdContracts.find { it.itemGroupCode == sapContract.itemGroupCode }
            } else {
                val contractToUpdate = contracts.first()

                val contractForUpdate = buildContractForUpdate(
                    contractToUpdate = contractToUpdate,
                    newData = sapContract,
                    sapSession = sapSession
                )

                val resourceUpdateUrl = "$resourceUrl%28${contractToUpdate.getAgreementNo()}%29"
                val wasApproved = contractToUpdate.getStatus() == "asApproved"
                val payload = jacksonObjectMapper().writeValueAsString(contractForUpdate)

                if (wasApproved) {
                    logger.info("Contract was approved, updating it to on hold")
                    updateContractStatus(
                        sapSession = sapSession,
                        resourceUrl = resourceUpdateUrl,
                        status = SapContractStatus.ON_HOLD
                    )
                }

                try {
                    updateSapEntity(
                        item = payload,
                        resourceUrl = resourceUpdateUrl,
                        sessionId = sapSession.sessionId,
                        routeId = sapSession.routeId
                    )

                    val updatedContracts = spreadContract(
                        sapSession = sapSession,
                        contract = contractForUpdate
                    )

                    return updatedContracts.find { it.itemGroupCode == sapContract.itemGroupCode }
                } finally {
                    if (wasApproved) {
                        logger.info("Contract was approved, updating it back to approved")
                        updateContractStatus(
                            sapSession = sapSession,
                            resourceUrl = resourceUpdateUrl,
                            status = SapContractStatus.APPROVED
                        )
                    }
                }
            }
        } catch (error: Exception) {
            logger.error("Error while creating SAP contract", error)
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
            firstResult = null,
            maxResults = null
        )

        return  sapListRequest(
            targetClass = Contract::class.java,
            requestUrl = requestUrl,
            sapSession = sapSession,
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
     * @param sapSession sap session
     * @param contracts contracts to spread
     * @return spread
     */
    private fun spreadContracts(
        sapSession: SapSession,
        contracts: List<Contract>
    ): List<SAPItemGroupContract> {
        return contracts.flatMap { contract ->
            spreadContract(
                sapSession = sapSession,
                contract = contract
            )
        }
    }

    /**
     * Spreads a contract to one contract per item group
     *
     * @param sapSession sap session
     * @param contract a contract to spread
     * @return spread contract
     */
    fun spreadContract(
        sapSession: SapSession,
        contract: Contract
    ): List<SAPItemGroupContract> {
        val itemGroupsWithQuantities = getContractItemGroupsWithQuantities(
            sapSession = sapSession,
            contract = contract
        )

        return itemGroupsWithQuantities.map { itemGroupWithQuantity ->
            val itemGroupCode = itemGroupWithQuantity.key
            val cumulativeQuantity = itemGroupWithQuantity.value

            SAPItemGroupContract(
                startDate = contract.getStartDate(),
                endDate = contract.getEndDate(),
                docNum = contract.getDocNum() ?: 0,
                bPCode = contract.getBpCode()!!,
                contactPersonCode = contract.getContactPersonCode(),
                status = contract.getStatus(),
                signingDate = contract.getSigningDate(),
                terminateDate = contract.getTerminateDate(),
                remarks = contract.getRemarks(),
                agreementNo = contract.getAgreementNo() ?: 0,
                cumulativeQuantity = cumulativeQuantity,
                itemGroupCode = itemGroupCode
            )
        }
    }

    /**
     * Returns map of contract item groups with cumulative quantities
     *
     * @param sapSession current active SAP session
     * @param contract contract
     * @return map of contract item groups with cumulative quantities
     */
    private fun getContractItemGroupsWithQuantities(
        sapSession: SapSession,
        contract: Contract
    ): Map<Int, Double> {
        val itemGroupsInContract = mutableMapOf<Int, Double>()
        val groupProperties = configController.getGroupPropertiesFromConfigFile()

        contract.getContractLines().forEachIndexed { index, contractLine ->
            val itemCode = contractLine.getItemNo()?.toInt()
            if (itemCode == null) {
                logger.error("Contract ${contract.getAgreementNo()} line $index does not have item code")
                return@forEachIndexed
            }

            val item = itemsController.findItem(
                sapSession = sapSession,
                itemCode = itemCode
            )

            if (item == null) {
                logger.error("Could not find item with code $itemCode")
                return@forEachIndexed
            }

            val itemGroupCode = itemsController.getItemGroupCode(item = item, groupProperties = groupProperties)
            if (itemGroupCode == null) {
                logger.error("Could not find item group for item $itemCode")
                return@forEachIndexed
            }

            val cumulativeQuantity = contractLine.getCumulativeQuantity() ?: 0.0
            if (!itemGroupsInContract.containsKey(itemGroupCode)) {
                itemGroupsInContract[itemGroupCode] = cumulativeQuantity
            } else {
                itemGroupsInContract[itemGroupCode]?.plus(cumulativeQuantity)
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
        val existingContractLines = contractToUpdate.getContractLines()
        val itemCodesToAdd = mutableListOf<String>()

        itemCodesToBeAdded.map { itemCode ->
            if (existingContractLines.find { existingContract -> existingContract.getItemNo() == itemCode } == null) {
                itemCodesToAdd.add(itemCode)
            }
        }

        val newLines = itemCodesToAdd.map { itemCode ->
            ContractLine(
                itemCode,
                1.0,
                0.0,
                null,
                mutableMapOf()
            )
        }

        val allLines = mutableListOf<ContractLine>()
        allLines.addAll(existingContractLines)
        allLines.addAll(newLines)

        return Contract(
            contractToUpdate.getStartDate(),
            contractToUpdate.getEndDate(),
            contractToUpdate.getDocNum(),
            contractToUpdate.getBpCode(),
            contractToUpdate.getContactPersonCode(),
            contractToUpdate.getStatus(),
            contractToUpdate.getSigningDate(),
            contractToUpdate.getTerminateDate(),
            contractToUpdate.getRemarks(),
            contractToUpdate.getAgreementNo(),
            allLines,
            contractToUpdate.getAdditionalFields()
        )
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
                code,
                1.0,
                0.0,
                null,
                mutableMapOf()
            )
        }

        return Contract(
            sapContract.startDate.toString(),
            sapContract.endDate.toString(),
            getDocNum(sapContract),
            sapContract.businessPartnerCode.toString(),
            sapContract.contactPersonCode,
            contractStatusToSapFormat(SapContractStatus.APPROVED),
            sapContract.signingDate.toString(),
            sapContract.terminateDate.toString(),
            sapContract.remarks,
            null,
            lines,
            mutableMapOf()
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
        return itemsController.listItems(
            sapSession = sapSession,
            itemGroupCode = groupCode,
            updatedAfter = null
        ).map(Item::itemCode)
    }

    /**
     * Reads doc nummer from sap contract
     *
     * @param sapContract SAP contract
     * @return doc number
     */
    private fun getDocNum(sapContract: SapContract): Int? {
        val id = sapContract.id ?: return null
        val parts = id.split("-")
        if (parts.size < 2) {
            return null
        }

        return parts[1].toIntOrNull()
    }

    /**
     * Updates SAP contract status
     *
     * @param sapSession a SAP session to use
     * @param resourceUrl SAP contract url
     * @param status new status
     */
    private fun updateContractStatus(sapSession: SapSession, resourceUrl: String, status: SapContractStatus) {
        val payload = jacksonObjectMapper().writeValueAsString(ContractStatus(status = contractStatusToSapFormat(status)))
        patchSapEntity(
            item = payload,
            resourceUrl = resourceUrl,
            sessionId = sapSession.sessionId,
            routeId = sapSession.routeId
        )
    }

}