package fi.metatavu.pakkasmarja.services.erp.sap

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.metatavu.pakkasmarja.services.erp.config.ConfigController
import fi.metatavu.pakkasmarja.services.erp.model.GroupProperty
import fi.metatavu.pakkasmarja.services.erp.model.Item
import fi.metatavu.pakkasmarja.services.erp.sap.exception.SapItemFetchException
import fi.metatavu.pakkasmarja.services.erp.sap.session.SapSession
import java.time.OffsetDateTime
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * SAP items controller
 *
 * @author Jari Nykänen
 */
@ApplicationScoped
class ItemsController: AbstractSapResourceController<Item>() {

    @Inject
    lateinit var configController: ConfigController

    /**
     * Lists items from SAP
     *
     * @param sapSession SAP session
     * @param itemGroupCode item group code
     * @param updatedAfter updated after
     * @param firstResult first result
     * @param maxResults max results
     * @return list of items
     */
    fun listItems(
        sapSession: SapSession,
        itemGroupCode: Int?,
        updatedAfter: OffsetDateTime?,
        firstResult: Int? = 0,
        maxResults: Int? = 9999
    ): List<Item> {
        val requestUrl = constructItemRequestUrl(
            sapSession = sapSession,
            updatedAfter = updatedAfter,
            itemGroupCode = itemGroupCode,
            firstResult = firstResult
        )

        return sapListRequest(
            targetClass = Item::class.java,
            requestUrl = requestUrl,
            sapSession = sapSession,
            maxResults = maxResults
        ) ?: return emptyList()
    }

    /**
     * Find item from SAP with given SAP ID
     *
     * @param sapSession SAP session
     * @param itemCode SAP item code
     * @return found sap item or null
     */
    fun findItem(
        sapSession: SapSession,
        itemCode: Int
    ): Item? {
        return findSapEntity(
            targetClass = Item::class.java,
            itemUrl = "${sapSession.apiUrl}/Items('$itemCode')",
            sessionId = sapSession.sessionId,
            path = sapSession.path
        )
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
     * Translates boolean value into sap string
     *
     * @param value boolean value
     * @return sap string
     */
    private fun getSapBool(value: Boolean): String {
        return if (value) "tYES" else "tNO"
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
        filterList.add("Properties64 eq SAPB1.BoYesNoEnum'tYES'")

        if (itemGroupCode != null) {
            val property = findItemGroupProperty(itemGroupCode)
            property ?: throw SapItemFetchException("Item group code $itemGroupCode not found")
            property.constraints.forEach { (propName, value) ->
                filterList.add("$propName eq SAPB1.BoYesNoEnum'${getSapBool(value)}'")
            }
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
        val itemProperties = getItemPropertyMap(item)

        group@ for (groupProperty in groupProperties) {
            for ((constraintName, expected) in groupProperty.constraints) {
                val property = constraintName.substringAfter("Properties").toIntOrNull()
                if (property == null) {
                    logger.error("Invalid property name $constraintName of ${groupProperty.displayName}")
                    continue@group
                }

                if (itemProperties[property] != expected) {
                    continue@group
                }
            }

            return groupProperty.code
        }

        logger.error("Could not find group code for item ${item.itemCode} with properties: ${jacksonObjectMapper().writeValueAsString(itemProperties)}, group properties: ${jacksonObjectMapper().writeValueAsString(groupProperties)}")

        return null
    }

    /**
     * Returns whether given item group code is valid or not
     *
     * @param itemGroupCode item group code
     * @return whether given item group code is valid or not
     */
    fun isValidItemGroupCode(itemGroupCode: Int): Boolean {
        return findItemGroupProperty(itemGroupCode) != null
    }

    /**
     * Finds item group property settings by group code
     *
     * @param itemGroupCode item group code or null
     * @return item group property settings
     */
    private fun findItemGroupProperty(itemGroupCode: Int): GroupProperty? {
        val groupProperties = configController.getGroupPropertiesFromConfigFile()
        return groupProperties.find { property -> property.code == itemGroupCode }
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

    /**
     * Reads item properties into map
     *
     * @param item item
     * @return item property map
     */
    private fun getItemPropertyMap(item: Item): Map<Int, Boolean> {
        return mapOf(
            1 to (item.properties1 == "tYES"),
            2 to (item.properties2 == "tYES"),
            3 to (item.properties3 == "tYES"),
            4 to (item.properties4 == "tYES"),
            5 to (item.properties5 == "tYES"),
            6 to (item.properties6 == "tYES"),
            7 to (item.properties7 == "tYES"),
            8 to (item.properties8 == "tYES"),
            9 to (item.properties9 == "tYES"),
            10 to (item.properties10 == "tYES"),
            11 to (item.properties11 == "tYES"),
            12 to (item.properties12 == "tYES"),
            13 to (item.properties13 == "tYES"),
            14 to (item.properties14 == "tYES"),
            15 to (item.properties15 == "tYES"),
            16 to (item.properties16 == "tYES"),
            17 to (item.properties17 == "tYES"),
            18 to (item.properties18 == "tYES"),
            19 to (item.properties19 == "tYES"),
            20 to (item.properties20 == "tYES"),
            21 to (item.properties21 == "tYES"),
            22 to (item.properties22 == "tYES"),
            23 to (item.properties23 == "tYES"),
            24 to (item.properties24 == "tYES"),
            25 to (item.properties25 == "tYES"),
            26 to (item.properties26 == "tYES"),
            27 to (item.properties27 == "tYES"),
            28 to (item.properties28 == "tYES"),
            29 to (item.properties29 == "tYES"),
            30 to (item.properties30 == "tYES"),
            31 to (item.properties31 == "tYES"),
            32 to (item.properties32 == "tYES"),
            33 to (item.properties33 == "tYES"),
            34 to (item.properties34 == "tYES"),
            35 to (item.properties35 == "tYES"),
            36 to (item.properties36 == "tYES"),
            37 to (item.properties37 == "tYES"),
            38 to (item.properties38 == "tYES"),
            39 to (item.properties39 == "tYES"),
            40 to (item.properties40 == "tYES"),
            41 to (item.properties41 == "tYES"),
            42 to (item.properties42 == "tYES"),
            43 to (item.properties43 == "tYES"),
            44 to (item.properties44 == "tYES"),
            45 to (item.properties45 == "tYES"),
            46 to (item.properties46 == "tYES"),
            47 to (item.properties47 == "tYES"),
            48 to (item.properties48 == "tYES"),
            49 to (item.properties49 == "tYES"),
            50 to (item.properties50 == "tYES"),
            51 to (item.properties51 == "tYES"),
            52 to (item.properties52 == "tYES"),
            53 to (item.properties53 == "tYES"),
            54 to (item.properties54 == "tYES"),
            55 to (item.properties55 == "tYES"),
            56 to (item.properties56 == "tYES"),
            57 to (item.properties57 == "tYES"),
            58 to (item.properties58 == "tYES"),
            59 to (item.properties59 == "tYES"),
            60 to (item.properties60 == "tYES"),
            61 to (item.properties61 == "tYES"),
            62 to (item.properties62 == "tYES"),
            63 to (item.properties63 == "tYES"),
            64 to (item.properties64 == "tYES")
        )
    }

}