package fi.metatavu.pakkasmarja.services.erp.model

data class GroupCode(
    val code: Int,
    val itemGroupPropertyName: String,
    val isFrozen: Boolean,
    val isOrganic: Boolean
)
