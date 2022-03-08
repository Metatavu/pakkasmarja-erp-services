package fi.metatavu.pakkasmarja.services.erp.impl.translate

import com.fasterxml.jackson.databind.JsonNode
import fi.metatavu.pakkasmarja.services.erp.api.model.SapItem
import fi.metatavu.pakkasmarja.services.erp.config.ConfigController
import fi.metatavu.pakkasmarja.services.erp.sap.ItemsController
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * The translator class for SAP contracts
 */
@ApplicationScoped
class ItemTranslator: AbstractTranslator<JsonNode, SapItem>() {

    @Inject
    lateinit var itemsController: ItemsController

    @Inject
    lateinit var configController: ConfigController

    override fun translate(nodes: List<JsonNode>): List<SapItem> {
        val groupCodes = configController.getGroupCodesFile()
        return nodes.map{ node -> this.translateSingleItem(node = node, groupCodes = groupCodes) }
    }

    override fun translate(node: JsonNode): SapItem {
        val itemGroupCode = itemsController.getItemGroupCode(item = node, groupCodes = configController.getGroupCodesFile())
        val updated = getUpdatedDateTime(node.get("UpdatedDate").asText(), node.get("UpdatedTime").asText())

        return SapItem(
            code = node.get("ItemCode").asInt(),
            itemGroupCode = itemGroupCode,
            name = node.get("ItemName").asText(),
            purchaseUnit = node.get("PurchaseUnit").asText(),
            batchManaged = node.get("ManageBatchNumbers").asText() == "tYES",
            updated = updated,
        )
    }

    /**
     * Translates single SAP item to spec SapItem
     *
     * @param node node to translate
     * @param groupCodes group codes
     * @return translated SapItem
     */
    private fun translateSingleItem(node: JsonNode, groupCodes: JsonNode): SapItem {
        val itemGroupCode = itemsController.getItemGroupCode(item = node, groupCodes = groupCodes)
        val updated = getUpdatedDateTime(node.get("UpdatedDate").asText(), node.get("UpdatedTime").asText())

        return SapItem(
            code = node.get("ItemCode").asInt(),
            itemGroupCode = itemGroupCode,
            name = node.get("ItemName").asText(),
            purchaseUnit = node.get("PurchaseUnit").asText(),
            batchManaged = node.get("ManageBatchNumbers").asText() == "tYES",
            updated = updated,
        )
    }
}