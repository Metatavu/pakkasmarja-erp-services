package fi.metatavu.pakkasmarja.services.erp.sap.session.exception

/**
 * Exception thrown when a SAP session login fails
 *
 * @param message error message
 */
class SapSessionLoginException(message: String): Exception(message) {
}