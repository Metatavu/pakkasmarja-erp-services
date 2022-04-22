package fi.metatavu.pakkasmarja.services.erp.impl

import fi.metatavu.pakkasmarja.services.erp.api.model.SapPurchaseDeliveryNote
import fi.metatavu.pakkasmarja.services.erp.api.spec.PurchaseDeliveryNotesApi
import fi.metatavu.pakkasmarja.services.erp.impl.translate.PurchaseDeliveryNoteTranslator
import fi.metatavu.pakkasmarja.services.erp.sap.PurchaseDeliveryNotesController
import fi.metatavu.pakkasmarja.services.erp.sap.session.SapSessionController
import javax.annotation.security.RolesAllowed
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.Response

/**
 * Stock transfers API implementation
 */
@RequestScoped
@Transactional
@Suppress("unused")
@RolesAllowed(UserRole.INTEGRATION.name)
class PurchaseDeliveryNotesApiImpl: PurchaseDeliveryNotesApi, AbstractApi() {

    @Inject
    lateinit var purchaseDeliveryNotesController: PurchaseDeliveryNotesController

    @Inject
    lateinit var purchaseDeliveryNoteTranslator: PurchaseDeliveryNoteTranslator

    @Inject
    lateinit var sapSessionController: SapSessionController

    override fun createPurchaseDeliveryNote(sapPurchaseDeliveryNote: SapPurchaseDeliveryNote): Response {
        val result = sapSessionController.createSapSession().use { sapSession ->
            purchaseDeliveryNotesController.createPurchaseDeliveryNote(
                sapSession = sapSession,
                sapPurchaseDeliveryNote = sapPurchaseDeliveryNote
            )
        } ?: return createInternalServerError("Failed to create stock transfer")

        return createOk(purchaseDeliveryNoteTranslator.translate(result))
    }

}