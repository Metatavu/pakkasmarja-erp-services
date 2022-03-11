package fi.metatavu.pakkasmarja.services.erp.sap

import fi.metatavu.pakkasmarja.services.erp.config.ConfigController
import fi.metatavu.pakkasmarja.services.erp.model.GroupCode
import fi.metatavu.pakkasmarja.services.erp.model.Item
import fi.metatavu.pakkasmarja.services.erp.sap.session.SapSession
import fi.metatavu.pakkasmarja.services.erp.sap.session.SapSessionController
import java.time.OffsetDateTime
import java.util.*
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
        firstResult: Int? = 0,
        maxResults: Int? = 9999
    ): List<Item> {
        sapSessionController.createSapSession().use { sapSession ->
            val requestUrl = constructItemRequestUrls(
                sapSession = sapSession,
                updatedAfter = updatedAfter,
                itemGroupCode = itemGroupCode,
                firstResult = firstResult
            ) ?: return emptyList()

            val itemsResponse = sapListRequest(
                requestUrl = requestUrl,
                sapSession = sapSession,
                maxResults = maxResults
            ) ?: return emptyList()

            return itemsResponse.map(this::convertToModel)
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
            val itemResponse = findItem(
                itemUrl = "${sapSession.apiUrl}/Items('$sapId')",
                sessionId = sapSession.sessionId,
                routeId = sapSession.routeId
            ) ?: return null

            return convertToModel<Item>(itemResponse)
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
     * @param groupCodes groups to use for selection
     * @return list of group property names
     */
    fun constructItemPropertiesList(groupCodes: List<GroupCode>): List<String> {
        return groupCodes.map { groupCode -> groupCode.itemGroupPropertyName }
    }

    /**
     * Creates a SAP query selector from property names list
     *
     * @param propertyNames list of property names
     * @return constructed query selector
     */
    fun getItemPropertiesSelect(propertyNames: List<String>): String {
        val combinedList = mutableListOf<String>()
        combinedList.addAll(propertyNames)
        combinedList.add("ItemCode")
        combinedList.add("Properties21")
        combinedList.add("Properties28")

        return "\$select=${combinedList.joinToString(",")}"
    }

    /**
     * Gets the group code of an item
     *
     * @param item item
     * @param groupCodes group codes config
     * @return group code or null
     */
    fun getItemGroupCode(item: Item, groupCodes: List<GroupCode>): Int? {
        val itemIsOrganic = item.properties21 == "tYES"
        val itemIsFrozen = item.properties28 == "tYES"

        groupCodes.forEach { groupCode ->
            val itemGroupPropertyName = groupCode.itemGroupPropertyName.lowercase(Locale.getDefault())
            val field = item.javaClass.getDeclaredField(itemGroupPropertyName)
            field.isAccessible = true
            val itemIsOfGroup = field.get(item) == "tYES"

            val groupIsFrozen = groupCode.isFrozen
            val groupIsOrganic = groupCode.isOrganic
            if (itemIsOfGroup && groupIsFrozen == itemIsFrozen && groupIsOrganic == itemIsOrganic) {
                return groupCode.code
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
    private fun constructItemSelect(itemGroupCode: Int?): String? {
        val allGroupCodes = configController.getGroupCodesFile()

        return if (itemGroupCode != null) {
            val foundItem = allGroupCodes.find { groupCode -> groupCode.code == itemGroupCode } ?: return null
            getItemPropertiesSelect(propertyNames = listOf(foundItem.itemGroupPropertyName))
        } else {
            val itemProperties = constructItemPropertiesList(groupCodes = allGroupCodes)
            getItemPropertiesSelect(propertyNames = itemProperties)
        }
    }

    /**
     * Constructs SAP item request URL
     *
     * @param sapSession current active SAP session
     * @param updatedAfter updated after filter or null
     * @param itemGroupCode item group code or null
     * @return list of SAP request URL
     */
    private fun constructItemRequestUrls(
        sapSession: SapSession,
        updatedAfter: OffsetDateTime?,
        itemGroupCode: Int?,
        firstResult: Int?
    ): String? {
        val baseUrl = "${sapSession.apiUrl}/Items"
        val select = constructItemSelect(itemGroupCode) ?: return null

        return if (updatedAfter == null) {
            constructSAPRequestUrl(
                baseUrl = baseUrl,
                select = select,
                filter = null,
                firstResult = firstResult
            )
        } else {
            val filter = "\$filter=${createdUpdatedAfterFilter(updatedAfter)}"

            constructSAPRequestUrl(
                baseUrl = baseUrl,
                select = select,
                filter = filter,
                firstResult = firstResult
            )
        }
    }

}