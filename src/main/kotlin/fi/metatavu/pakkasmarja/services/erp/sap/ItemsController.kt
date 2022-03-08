package fi.metatavu.pakkasmarja.services.erp.sap

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.pakkasmarja.services.erp.api.model.SapItem
import fi.metatavu.pakkasmarja.services.erp.config.ConfigController
import fi.metatavu.pakkasmarja.services.erp.sap.session.SapSession
import fi.metatavu.pakkasmarja.services.erp.sap.session.SapSessionController
import kotlinx.coroutines.future.await
import java.time.OffsetDateTime
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.context.RequestScoped
import javax.inject.Inject

/**
 * SAP items controller
 *
 * @author Jari Nykänen
 */
@RequestScoped
class ItemsController: AbstractSapResourceController() {

    @Inject
    lateinit var sapSessionController: SapSessionController

    @Inject
    lateinit var configController: ConfigController

    fun listItems(
        itemGroupCode: Int?,
        updatedAfter: OffsetDateTime?,
        firstResult: Int?,
        maxResults: Int?
    ): List<JsonNode> {
        sapSessionController.createSapSession().use{ sapSession ->
            return getItemsRequest(
                itemUrls = constructUrls(sapSession = sapSession, updatedAfter = updatedAfter, itemGroupCode = itemGroupCode),
                sapSession = sapSession,
                maxResults = maxResults
            )
        }
    }

    /**
     * Find item from SAP with given SAP ID
     *
     * @param sapId SAP ID to search
     * @return found sap item or null
     */
    fun findItem(sapId: Int): JsonNode? {
        sapSessionController.createSapSession().use{ sapSession ->
            return findItem(
                itemUrl = "${sapSession.apiUrl}/Items('$sapId')",
                sessionId = sapSession.sessionId,
                routeId = sapSession.routeId
            )
        }
    }

    /**
     *
     */
    fun findItemFromItemList(items: List<JsonNode>, itemCode: String?): JsonNode? {
        return items.find { item -> item.get("ItemCode").asText() == itemCode }
    }

    /**
     * Creates a property name list from group codes
     *
     * @param groupCodes groups to use for selection
     * @return list of group property names
     */
    fun constructItemPropertiesList(groupCodes: JsonNode?): List<String> {
        return (groupCodes ?: configController.getGroupCodesFile())
            .map { groupCode -> groupCode.get("itemGroupPropertyName").asText() }
    }

    /**
     * Creates a SAP query selector from property names list
     *
     * @param propertyNames list of property names
     * @return constructed query selector
     */
    fun getItemPropertiesSelect(propertyNames: List<String>): String {
        return "\$select=${propertyNames.joinToString(",").plus(",ItemCode,Properties28,Properties35")}"
    }

    /**
     * Gets the group code of an item
     *
     * @param item item
     * @param groupCodes group codes config
     * @return group code or null
     */
    fun getItemGroupCode(item: JsonNode, groupCodes: JsonNode): Int? {
        val itemIsFrozen = item.get("Properties28").asText() == "tYES"
        val itemIsOrganic = item.get("Properties35").asText() == "tYES"

        groupCodes.fields().forEach { pair ->
            val groupCode = pair.value
            val itemGroupPropertyName = groupCode.get("itemGroupPropertyName").asText()
            val itemIsOfGroup = item.get(itemGroupPropertyName).asText() == "tYES"
            val groupIsFrozen = groupCode.get("isFrozen").asBoolean()
            val groupIsOrganic = groupCode.get("isOrganic").asBoolean()
            if (itemIsOfGroup && groupIsFrozen == itemIsFrozen && groupIsOrganic == itemIsOrganic) {
                return pair.key.toInt()
            }
        }

        return null
    }

    private fun constructItemSelect(itemGroupCode: Int?): String {
        val allGroupCodes = configController.getGroupCodesFile()

        return if (itemGroupCode != null) {
            val foundItem = allGroupCodes.findValue(itemGroupCode.toString()) ?: return ""
            val itemGroupPropertyName = foundItem.get("itemGroupPropertyName").asText() ?: ""
            getItemPropertiesSelect(propertyNames = listOf(itemGroupPropertyName))
        } else {
            val itemProperties = constructItemPropertiesList(groupCodes = allGroupCodes)
            getItemPropertiesSelect(propertyNames = itemProperties)
        }
    }

    private fun constructUrls(
        sapSession: SapSession,
        updatedAfter: OffsetDateTime?,
        itemGroupCode: Int?
    ): List<String> {
        val resourceUrl = "${sapSession.apiUrl}/Items"
        val sessionId = sapSession.sessionId
        val routeId = sapSession.routeId

        return if (updatedAfter == null) {
            constructSAPUrl(
                resourceUrl = resourceUrl,
                sessionId = sessionId,
                routeId = routeId,
                select = constructItemSelect(itemGroupCode)
            )
        } else {
            constructSAPUrlWithFilter(
                resourceUrl = resourceUrl,
                sessionId = sessionId,
                routeId = routeId,
                select = constructItemSelect(itemGroupCode),
                filter = "\$filter=${createdUpdatedAfterFilter(updatedAfter)}",
            )
        }
    }

}