package fi.metatavu.pakkasmarja.services.erp.impl

import fi.metatavu.pakkasmarja.services.erp.api.spec.ItemsApi
import fi.metatavu.pakkasmarja.services.erp.impl.translate.ItemTranslator
import fi.metatavu.pakkasmarja.services.erp.sap.ItemsController
import fi.metatavu.pakkasmarja.services.erp.sap.session.SapSessionController
import io.quarkus.security.Authenticated
import java.time.OffsetDateTime
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.Response

/**
 * Items API implementation
 *
 * @author Jari Nykänen
 */
@RequestScoped
@Transactional
@Suppress("unused")
@Authenticated
class ItemsApiImpl: ItemsApi, AbstractApi() {

    @Inject
    lateinit var itemsController: ItemsController

    @Inject
    lateinit var itemTranslator: ItemTranslator

    @Inject
    lateinit var sapSessionController: SapSessionController

    override fun listItems(
        itemGroupCode: Int?,
        updatedAfter: OffsetDateTime?,
        firstResult: Int?,
        maxResults: Int?
    ): Response {
        if (itemGroupCode != null && !itemsController.isValidItemGroupCode(itemGroupCode = itemGroupCode)) {
             return createBadRequest("Item group code $itemGroupCode is not valid")
        }

        val items = sapSessionController.createSapSession().use { sapSession ->
            itemsController.listItems(
                sapSession = sapSession,
                itemGroupCode = itemGroupCode,
                updatedAfter = updatedAfter,
                firstResult = firstResult,
                maxResults = maxResults
            )
        }

        return createOk(items.map(itemTranslator::translate))
    }

    override fun findItem(sapId: Int): Response {
        val foundItem = sapSessionController.createSapSession().use { sapSession ->
            itemsController.findItem(
                sapSession = sapSession,
                itemCode = sapId
            ) ?: return createNotFound("Sap item with ID: $sapId could not be found")
        }

        return createOk(itemTranslator.translate(foundItem))
    }

}