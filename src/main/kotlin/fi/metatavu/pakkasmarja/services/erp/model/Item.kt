package fi.metatavu.pakkasmarja.services.erp.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.quarkus.runtime.annotations.RegisterForReflection

/**
 * Data class for Item
 *
 * @author Jari Nykänen
 */
@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
data class Item(
    @JsonProperty("ItemCode")
    val itemCode: String,
    @JsonProperty("ItemName")
    val itemName: String?,
    @JsonProperty("ManageBatchNumbers")
    val manageBatchNumbers: String?,
    @JsonProperty("PurchaseUnit")
    val purchaseUnit: String?,
    @JsonProperty("PurchaseItemsPerUnit")
    val purchaseItemsPerUnit: Double?,
    @JsonProperty("PurchaseQtyPerPackUnit")
    val purchaseQtyPerPackUnit: Double?,
    @JsonProperty("Properties1")
    val properties1: String?,
    @JsonProperty("Properties2")
    val properties2: String?,
    @JsonProperty("Properties3")
    val properties3: String?,
    @JsonProperty("Properties4")
    val properties4: String?,
    @JsonProperty("Properties5")
    val properties5: String?,
    @JsonProperty("Properties6")
    val properties6: String?,
    @JsonProperty("Properties7")
    val properties7: String?,
    @JsonProperty("Properties8")
    val properties8: String?,
    @JsonProperty("Properties9")
    val properties9: String?,
    @JsonProperty("Properties10")
    val properties10: String?,
    @JsonProperty("Properties11")
    val properties11: String?,
    @JsonProperty("Properties12")
    val properties12: String?,
    @JsonProperty("Properties13")
    val properties13: String?,
    @JsonProperty("Properties14")
    val properties14: String?,
    @JsonProperty("Properties15")
    val properties15: String?,
    @JsonProperty("Properties16")
    val properties16: String?,
    @JsonProperty("Properties17")
    val properties17: String?,
    @JsonProperty("Properties18")
    val properties18: String?,
    @JsonProperty("Properties19")
    val properties19: String?,
    @JsonProperty("Properties20")
    val properties20: String?,
    @JsonProperty("Properties21")
    val properties21: String?,
    @JsonProperty("Properties22")
    val properties22: String?,
    @JsonProperty("Properties23")
    val properties23: String?,
    @JsonProperty("Properties24")
    val properties24: String?,
    @JsonProperty("Properties25")
    val properties25: String?,
    @JsonProperty("Properties26")
    val properties26: String?,
    @JsonProperty("Properties27")
    val properties27: String?,
    @JsonProperty("Properties28")
    val properties28: String?,
    @JsonProperty("Properties29")
    val properties29: String?,
    @JsonProperty("Properties30")
    val properties30: String?,
    @JsonProperty("Properties31")
    val properties31: String?,
    @JsonProperty("Properties32")
    val properties32: String?,
    @JsonProperty("Properties33")
    val properties33: String?,
    @JsonProperty("Properties34")
    val properties34: String?,
    @JsonProperty("Properties35")
    val properties35: String?,
    @JsonProperty("Properties36")
    val properties36: String?,
    @JsonProperty("Properties37")
    val properties37: String?,
    @JsonProperty("Properties38")
    val properties38: String?,
    @JsonProperty("Properties39")
    val properties39: String?,
    @JsonProperty("Properties40")
    val properties40: String?,
    @JsonProperty("Properties41")
    val properties41: String?,
    @JsonProperty("Properties42")
    val properties42: String?,
    @JsonProperty("Properties43")
    val properties43: String?,
    @JsonProperty("Properties44")
    val properties44: String?,
    @JsonProperty("Properties45")
    val properties45: String?,
    @JsonProperty("Properties46")
    val properties46: String?,
    @JsonProperty("Properties47")
    val properties47: String?,
    @JsonProperty("Properties48")
    val properties48: String?,
    @JsonProperty("Properties49")
    val properties49: String?,
    @JsonProperty("Properties50")
    val properties50: String?,
    @JsonProperty("Properties51")
    val properties51: String?,
    @JsonProperty("Properties52")
    val properties52: String?,
    @JsonProperty("Properties53")
    val properties53: String?,
    @JsonProperty("Properties54")
    val properties54: String?,
    @JsonProperty("Properties55")
    val properties55: String?,
    @JsonProperty("Properties56")
    val properties56: String?,
    @JsonProperty("Properties57")
    val properties57: String?,
    @JsonProperty("Properties58")
    val properties58: String?,
    @JsonProperty("Properties59")
    val properties59: String?,
    @JsonProperty("Properties60")
    val properties60: String?,
    @JsonProperty("Properties61")
    val properties61: String?,
    @JsonProperty("Properties62")
    val properties62: String?,
    @JsonProperty("Properties63")
    val properties63: String?,
    @JsonProperty("Properties64")
    val properties64: String?,
    @JsonProperty("DefaultPurchasingUoMEntry")
    val defaultPurchasingUoMEntry: Any?,
    @JsonProperty("UpdateDate")
    val updateDate: String?,
    @JsonProperty("UpdateTime")
    val updateTime: String?
)