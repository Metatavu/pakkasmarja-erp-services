package fi.metatavu.pakkasmarja.services.erp.impl

import fi.metatavu.pakkasmarja.services.erp.api.spec.ItemsApi
import fi.metatavu.pakkasmarja.services.erp.impl.translate.ItemTranslator
import fi.metatavu.pakkasmarja.services.erp.sap.ItemsController
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

    override fun listItems(
        itemGroupCode: Int?,
        updatedAfter: OffsetDateTime?,
        firstResult: Int?,
        maxResults: Int?
    ): Response {
        val items = itemsController.listItems(
            itemGroupCode = itemGroupCode,
            updatedAfter = updatedAfter,
            firstResult = firstResult,
            maxResults = maxResults
        )

        return createOk(itemTranslator.translate(items))
    }

    override fun findItem(sapId: Int): Response {
        val foundItem = itemsController.findItem(sapId = sapId) ?: return createNotFound("Sap item with ID: $sapId could not be found")
        return createOk(itemTranslator.translate(foundItem))
    }

}