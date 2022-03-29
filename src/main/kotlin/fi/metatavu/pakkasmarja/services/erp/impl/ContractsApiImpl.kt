package fi.metatavu.pakkasmarja.services.erp.impl

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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
@Suppress("unused")
@Authenticated
class ContractsApiImpl: ContractsApi, AbstractApi() {

    @Inject
    lateinit var contractsController: ContractsController

    @Inject
    lateinit var contractTranslator: ContractTranslator

    override fun listContracts(
        startDate: LocalDate?,
        businessPartnerCode: String?,
        contractStatus: SapContractStatus?
    ): Response {
        println("contracts startDate: ${startDate}")
        println("contracts businessPartnerCode: ${businessPartnerCode}")
        println("contracts contractStatus: ${contractStatus}")

        val contracts = contractsController.listContracts(
            startDate = startDate,
            businessPartnerCode = businessPartnerCode,
            contractStatus = contractStatus
        )

        println("contractsit:")
        println(jacksonObjectMapper().writeValueAsString(contracts))

        val translatedContracts = contracts.map(contractTranslator::translate)

        println("translatedContractsit:")
        println(jacksonObjectMapper().writeValueAsString(translatedContracts))

        return createOk(translatedContracts)
    }

    override fun createContract(sapContract: SapContract): Response {
        val newContract = contractsController.createContract(sapContract = sapContract) ?: return createInternalServerError("Error while creating contract")
        return createOk(contractTranslator.translate(newContract))
    }

    override fun findContract(sapId: String): Response {
        TODO("Not yet implemented")
    }

}