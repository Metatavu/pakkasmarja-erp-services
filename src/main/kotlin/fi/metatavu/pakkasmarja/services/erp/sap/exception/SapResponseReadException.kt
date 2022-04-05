package fi.metatavu.pakkasmarja.services.erp.sap.exception

/**
 * Exception thrown when reading response from SAP fails
 *
 * @param message message
 */
class SapResponseReadException(message: String): Exception(message) {
}