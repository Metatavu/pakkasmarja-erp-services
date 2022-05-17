package fi.metatavu.pakkasmarja.services.erp.impl

import fi.metatavu.pakkasmarja.services.erp.api.model.SapContract
import fi.metatavu.pakkasmarja.services.erp.api.model.SapContractStatus
import fi.metatavu.pakkasmarja.services.erp.api.spec.ContractsApi
import fi.metatavu.pakkasmarja.services.erp.impl.translate.ContractTranslator
import fi.metatavu.pakkasmarja.services.erp.sap.ContractsController
import fi.metatavu.pakkasmarja.services.erp.sap.ItemsController
import fi.metatavu.pakkasmarja.services.erp.sap.session.SapSessionController
import java.time.LocalDate
import javax.annotation.security.RolesAllowed
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
@RolesAllowed(UserRole.INTEGRATION.name)
class ContractsApiImpl: ContractsApi, AbstractApi() {

    @Inject
    lateinit var contractsController: ContractsController

    @Inject
    lateinit var contractTranslator: ContractTranslator

    @Inject
    lateinit var sapSessionController: SapSessionController

    @Inject
    lateinit var itemsController: ItemsController

    override fun listContracts(
        startDate: LocalDate?,
        businessPartnerCode: String?,
        contractStatus: SapContractStatus?
    ): Response {
        val contracts = sapSessionController.createSapSession().use { sapSession ->
            contractsController.listContracts(
                sapSession = sapSession,
                startDate = startDate,
                businessPartnerCode = businessPartnerCode,
                contractStatus = contractStatus
            )
        }

        return createOk(contracts.map(contractTranslator::translate))
    }

    override fun createContract(sapContract: SapContract): Response {
        if (!itemsController.isValidItemGroupCode(itemGroupCode = sapContract.itemGroupCode)) {
            return createBadRequest("Item group code ${sapContract.itemGroupCode} is not valid")
        }

        val newContract = sapSessionController.createSapSession().use { sapSession ->
            contractsController.createContract(
                sapSession = sapSession,
                sapContract = sapContract
            )
        } ?: return createInternalServerError("Error while creating contract")

        return createOk(contractTranslator.translate(newContract))
    }

    override fun findContract(sapId: String): Response {
        TODO("Not yet implemented")
    }

}