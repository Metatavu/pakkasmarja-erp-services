package fi.metatavu.pakkasmarja.services.erp.sap.exception

/**
 * Thrown when creating, updating, or deleting from SAP fails
 */
class SapModificationException(message: String): Exception(message) {
}