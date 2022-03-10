package fi.metatavu.pakkasmarja.services.erp.impl.translate

import fi.metatavu.pakkasmarja.services.erp.api.model.SapItem
import fi.metatavu.pakkasmarja.services.erp.config.ConfigController
import fi.metatavu.pakkasmarja.services.erp.model.Item
import fi.metatavu.pakkasmarja.services.erp.sap.ItemsController
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * The translator class for SAP contracts
 */
@ApplicationScoped
class ItemTranslator: AbstractTranslator<Item, SapItem>() {

    @Inject
    lateinit var itemsController: ItemsController

    @Inject
    lateinit var configController: ConfigController

    override fun translate(nodes: List<Item>): List<SapItem> {
        TODO("Not implemented yet")
    }

    override fun translate(node: Item): SapItem {
        val itemGroupCode = itemsController.getItemGroupCode(item = node, groupCodes = configController.getGroupCodesFile())
        val updated = getUpdatedDateTime(node.updateDate, node.updateTime)

        return SapItem(
            code = node.itemCode.toInt(),
            itemGroupCode = itemGroupCode,
            name = node.itemName,
            purchaseUnit = node.purchaseUnit ?: "",
            batchManaged = node.manageBatchNumbers == "tYES",
            updated = updated,
        )
    }

}