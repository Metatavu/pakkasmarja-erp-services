package fi.metatavu.pakkasmarja.services.erp.impl.translate

import java.time.*

/**
 * Abstract translator class
 *
 * @author Jari Nykänen
 */
abstract class AbstractTranslator<E, R> {

    abstract fun translate(sapEntity: E): R

    /**
     * Translates list of entities
     *
     * @param sapEntities list of SAP entities to translate
     * @return List of translated nodes
     */
    open fun translate(sapEntities: List<E>): List<R> {
        return sapEntities.map(this::translate)
    }

    /**
     * Combines UpdateDate and UpdateTime from SAP into a single OffsetDateTime-object
     *
     * @param updatedDate updated date
     * @param updatedTime updated time
     * @param zoneId zone ID string
     * @return updated datetime
     */
    protected fun getUpdatedDateTime(updatedDate: String, updatedTime: String, zoneId: String? = "Europe/Helsinki"): OffsetDateTime {
        val date = LocalDate.parse(updatedDate)
        val time = LocalTime.parse(updatedTime)
        val zone = ZoneId.of(zoneId)
        val zoneOffset = zone.rules.getOffset(LocalDateTime.now())
        return OffsetDateTime.of(date, time, zoneOffset)
    }

    /**
     * Tries to parse a string to LocalDate and returns null if fails
     *
     * @param date string to parse
     * @return parsed string or null
     */
    protected fun resolveLocalDate (date: String?): LocalDate? {
        return try {
            LocalDate.parse(date)
        } catch (e: Exception) {
            null
        }
    }

}