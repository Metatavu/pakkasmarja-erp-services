package fi.metatavu.pakkasmarja.services.erp.sap.utils

/**
 * Exception thrown when fetching items from SAP fails
 *
 * @param message message
 */
class SapItemFetchException(message: String): Exception(message) {
}