package fi.metatavu.pakkasmarja.services.erp.sap

import fi.metatavu.pakkasmarja.services.erp.config.ConfigController
import fi.metatavu.pakkasmarja.services.erp.model.GroupProperty
import fi.metatavu.pakkasmarja.services.erp.model.Item
import fi.metatavu.pakkasmarja.services.erp.sap.session.SapSession
import fi.metatavu.pakkasmarja.services.erp.sap.session.SapSessionController
import java.time.OffsetDateTime
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * SAP items controller
 *
 * @author Jari Nykänen
 */
@ApplicationScoped
class ItemsController: AbstractSapResourceController() {

    @Inject
    lateinit var sapSessionController: SapSessionController

    @Inject
    lateinit var configController: ConfigController

    fun listItems(
        itemGroupCode: Int?,
        updatedAfter: OffsetDateTime?,
        firstResult: Int? = 0,
        maxResults: Int? = 9999
    ): List<Item> {
        sapSessionController.createSapSession().use { sapSession ->
            val requestUrl = constructItemRequestUrl(
                sapSession = sapSession,
                updatedAfter = updatedAfter,
                itemGroupCode = itemGroupCode,
                firstResult = firstResult
            )

            return sapListItemsRequest(
                requestUrl = requestUrl,
                sapSession = sapSession,
                maxResults = maxResults
            ) ?: return emptyList()
        }
    }

    /**
     * Find item from SAP with given SAP ID
     *
     * @param sapId SAP ID to search
     * @return found sap item or null
     */
    fun findItem(sapId: Int): Item? {
        sapSessionController.createSapSession().use { sapSession ->
            return findSapEntity(
                targetClass = Item::class.java,
                itemUrl = "${sapSession.apiUrl}/Items('$sapId')",
                sessionId = sapSession.sessionId,
                routeId = sapSession.routeId
            )
        }
    }

    /**
     * Find item from item list
     *
     * @param items list of items
     * @param itemCode item code to search for
     * @return found item or null
     */
    fun findItemFromItemList(items: List<Item>, itemCode: String?): Item? {
        return items.find { item -> item.itemCode == itemCode }
    }

    /**
     * Creates a property name list from group codes
     *
     * @param groupProperties groups to use for selection
     * @return list of group property names
     */
    fun constructItemPropertiesList(groupProperties: List<GroupProperty>): List<String> {
        return groupProperties.map { groupProperty -> groupProperty.itemGroupPropertyName }
    }

    /**
     * Creates a SAP query selector from property names list
     *
     * @return constructed query selector
     */
    fun getItemPropertiesSelect(): String {
        val combinedList = mutableListOf("ItemCode", "ItemName", "UpdateTime", "UpdateDate")
        for (i in 1..64) {
            combinedList.add("Properties$i")
        }

        return "\$select=${combinedList.joinToString(",")}"
    }

    /**
     * Constructs filter for SAP request
     *
     * @param updatedAfter updated after date or null
     * @param itemGroupCode item group code or null
     * @return constructed filter string or null
     */
    private fun constructFilter(updatedAfter: OffsetDateTime?, itemGroupCode: Int?): String? {
        val filterList = mutableListOf<String>()

        if (itemGroupCode != null) {
            val property = findItemPropertyFilterByGroupCode(itemGroupCode)
            filterList.add("$property eq 'tYES'")
        }

        if (updatedAfter != null) {
            filterList.add(createdUpdatedAfterFilter(updatedAfter))
        }

        if (filterList.size > 0) {
            return "\$filter=${filterList.joinToString(" and ")}"
        }

        return null
    }

    /**
     * Gets the group code of an item
     *
     * @param item item
     * @param groupProperties list of group properties
     * @return group code or null
     */
    fun getItemGroupCode(item: Item, groupProperties: List<GroupProperty>): Int? {
        val itemIsOrganic = item.properties21 == "tYES"
        val itemIsFrozen = item.properties28 == "tYES"

        groupProperties.forEach { groupProperty ->
            val itemGroupPropertyName = groupProperty.itemGroupPropertyName.lowercase(Locale.getDefault())
            val field = item.javaClass.getDeclaredField(itemGroupPropertyName)
            field.isAccessible = true
            val itemIsOfGroup = field.get(item) == "tYES"

            val groupIsFrozen = groupProperty.isFrozen
            val groupIsOrganic = groupProperty.isOrganic
            if (itemIsOfGroup && groupIsFrozen == itemIsFrozen && groupIsOrganic == itemIsOrganic) {
                return groupProperty.code
            }
        }

        return null
    }

    /**
     * Constructs item select string
     *
     * @param itemGroupCode item group code or null
     * @return constructed select string or null if something failed during construction
     */
    private fun findItemPropertyFilterByGroupCode(itemGroupCode: Int): String {
        val groupProperties = configController.getGroupPropertiesFromConfigFile()
        val foundGroup = groupProperties.find { property -> property.code == itemGroupCode } ?: return ""
        return foundGroup.itemGroupPropertyName
    }

    /**
     * Constructs SAP item request URL
     *
     * @param sapSession current active SAP session
     * @param updatedAfter updated after filter or null
     * @param itemGroupCode item group code or null
     * @param firstResult first result or null
     * @return list of SAP request URL
     */
    private fun constructItemRequestUrl(
        sapSession: SapSession,
        updatedAfter: OffsetDateTime?,
        itemGroupCode: Int?,
        firstResult: Int?
    ): String {
        val baseUrl = "${sapSession.apiUrl}/Items"
        val select = getItemPropertiesSelect()
        val filter = constructFilter(updatedAfter = updatedAfter, itemGroupCode = itemGroupCode)

        return constructSAPRequestUrl(
            baseUrl = baseUrl,
            select = select,
            filter = filter,
            firstResult = firstResult
        )
    }

}