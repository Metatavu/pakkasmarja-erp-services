package fi.metatavu.pakkasmarja.services.erp.sap.session

/**
 * Exception thrown when a SAP session logout fails
 *
 * @param message error message
 */
class SapSessionLogoutException(message: String): Exception(message) {
}