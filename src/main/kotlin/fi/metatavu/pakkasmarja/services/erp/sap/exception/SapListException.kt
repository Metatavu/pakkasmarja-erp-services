package fi.metatavu.pakkasmarja.services.erp.sap.exception

/**
 * Exception thrown when listing entries from SAP fails
 *
 * @param message message
 */
class SapListException(message: String): Exception(message) {
}