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

    override fun translate(sapEntities: List<SAPItemGroupContract>): List<SapContract> {
        TODO("Not yet implemented")
    }

    /**
     * Translates a contract from SAP into tbe format expected by spec
     *
     * @param sapEntity a contract from SAP
     * @return translated contract
     */
    override fun translate(sapEntity: SAPItemGroupContract): SapContract {
        val startDate = resolveLocalDate(sapEntity.startDate)
        val year = startDate?.year.toString()

        return SapContract(
            id = "$year-${sapEntity.docNum}-${sapEntity.itemGroupCode}",
            businessPartnerCode = sapEntity.bPCode.toInt(),
            contactPersonCode = sapEntity.contactPersonCode?: 0,
            itemGroupCode = sapEntity.itemGroupCode ?: 0,
            status = resolveContractStatus(sapEntity.status)!!,
            deliveredQuantity = sapEntity.cumulativeQuantity,
            startDate = startDate,
            endDate = resolveLocalDate(sapEntity.endDate),
            signingDate = resolveLocalDate(sapEntity.signingDate),
            terminateDate = resolveLocalDate(sapEntity.terminateDate),
            remarks = sapEntity.remarks
        )
    }

    /**
     * Translates a contract status from SAP into the format expect by spec
     *
     * @param status status from SAP
     * @return translated status
     */
    private fun resolveContractStatus(status: String?): SapContractStatus? {
        return when (status) {
            "asApproved" -> SapContractStatus.APPROVED
            "asDraft" -> SapContractStatus.DRAFT
            "asOnHold" -> SapContractStatus.ON_HOLD
            "asTerminated" -> SapContractStatus.TERMINATED
            else -> null
        }
    }

}