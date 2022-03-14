package fi.metatavu.pakkasmarja.services.erp.impl.translate

import fi.metatavu.pakkasmarja.services.erp.api.model.SapItem
import fi.metatavu.pakkasmarja.services.erp.config.ConfigController
import fi.metatavu.pakkasmarja.services.erp.model.Item
import fi.metatavu.pakkasmarja.services.erp.sap.ItemsController
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * The translator class for SAP contracts
 *
 * @author Jari Nykänen
 */
@ApplicationScoped
class ItemTranslator: AbstractTranslator<Item, SapItem>() {

    @Inject
    lateinit var itemsController: ItemsController

    @Inject
    lateinit var configController: ConfigController

    override fun translate(sapEntities: List<Item>): List<SapItem> {
        TODO("Not implemented yet")
    }

    override fun translate(sapEntity: Item): SapItem {
        val itemGroupCode = itemsController.getItemGroupCode(item = sapEntity, groupProperties = configController.getGroupPropertiesFromConfigFile())
        val updated = getUpdatedDateTime(sapEntity.updateDate, sapEntity.updateTime)

        return SapItem(
            code = sapEntity.itemCode.toInt(),
            itemGroupCode = itemGroupCode,
            name = sapEntity.itemName,
            purchaseUnit = sapEntity.purchaseUnit ?: "",
            batchManaged = sapEntity.manageBatchNumbers == "tYES",
            updated = updated,
        )
    }

}