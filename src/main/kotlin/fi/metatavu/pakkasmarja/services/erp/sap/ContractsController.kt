package fi.metatavu.pakkasmarja.services.erp.sap

import com.fasterxml.jackson.databind.JsonNode
import fi.metatavu.pakkasmarja.services.erp.api.model.SapContractStatus
import fi.metatavu.pakkasmarja.services.erp.sap.session.SapSessionController
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
            val combinedFilter = mutableListOf(startDateFilter, businessPartnerCodeFilter, contractStatusFilter).filterNotNull().joinToString(" and ")
            val filter = "\$filter=$combinedFilter"

            return getItemsAsJsonNodes(
                resourceUrl = resourceUrl,
                filter = filter,
                select = "\$select=*",
                routeId = sapSession.routeId,
                sessionId = sapSession.sessionId
            )
        }
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