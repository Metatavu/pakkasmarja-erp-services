package fi.metatavu.pakkasmarja.services.erp.impl

/**
 * Sealed class for user roles
 */
sealed class UserRole() {

    object INTEGRATION: UserRole() {
        const val name = "erp-integration"
    }

}