package fi.metatavu.pakkasmarja.services.erp.impl

import fi.metatavu.pakkasmarja.services.erp.api.model.SapContract
import fi.metatavu.pakkasmarja.services.erp.api.model.SapContractStatus
import fi.metatavu.pakkasmarja.services.erp.api.spec.ContractsApi
import fi.metatavu.pakkasmarja.services.erp.impl.translate.ContractTranslator
import fi.metatavu.pakkasmarja.services.erp.sap.ContractsController
import io.quarkus.security.Authenticated
import java.time.LocalDate
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.Response

/**
 * Contracts API implementation
 */
@RequestScoped
@Transactional
class ContractsApiImpl: ContractsApi, AbstractApi() {

    @Inject
    private lateinit var contractsController: ContractsController

     @Inject
     private lateinit var contractTranslator: ContractTranslator

    override fun createContract(sapContract: SapContract): Response {
        TODO("Not yet implemented")
    }

    override fun deleteContract(sapId: String): Response {
        TODO("Not yet implemented")
    }

    override fun findContract(sapId: String): Response {
        TODO("Not yet implemented")
    }

    @Authenticated
    override fun listContracts(
        startDate: LocalDate?,
        businessPartnerCode: String?,
        contractStatus: SapContractStatus?
    ): Response {
        val contracts = contractsController.listContracts(
            startDate = startDate,
            businessPartnerCode = businessPartnerCode,
            contractStatus = contractStatus
        )
        val translatedContracts = contracts.mapNotNull(contractTranslator::translate)

        return createOk(translatedContracts)
    }

    override fun updateContract(sapId: String, sapContract: SapContract): Response {
        TODO("Not yet implemented")
    }
}