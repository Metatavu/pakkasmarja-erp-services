package fi.metatavu.pakkasmarja.services.erp.impl.translate

import fi.metatavu.pakkasmarja.services.erp.api.model.SapContract
import fi.metatavu.pakkasmarja.services.erp.api.model.SapContractStatus
import fi.metatavu.pakkasmarja.services.erp.model.SAPItemGroupContract
import javax.enterprise.context.ApplicationScoped

/**
 * The translator class for SAP contracts
 */
@ApplicationScoped
class ContractTranslator: AbstractTranslator<SAPItemGroupContract, SapContract>() {

    override fun translate(nodes: List<SAPItemGroupContract>): List<SapContract> {
        TODO("Not yet implemented")
    }

    /**
     * Translates a contract from SAP into tbe format expected by spec
     *
     * @param node a contract from SAP
     * @return translated contract
     */
    override fun translate(node: SAPItemGroupContract): SapContract {
        val startDate = resolveLocalDate(node.startDate)
        val year = startDate?.year.toString()

        return SapContract(
            id = "$year-${node.docNum}",
            businessPartnerCode = node.bPCode.toInt(),
            contactPersonCode = node.contactPersonCode,
            itemGroupCode = node.itemGroupCode,
            status = resolveContractStatus(node.status)!!,
            deliveredQuantity = node.cumulativeQuantity,
            startDate = startDate,
            endDate = resolveLocalDate(node.endDate),
            signingDate = resolveLocalDate(node.signingDate),
            terminateDate = resolveLocalDate(node.terminateDate),
            remarks = node.remarks
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