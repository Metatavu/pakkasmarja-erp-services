package fi.metatavu.pakkasmarja.services.erp.jaxrs

import java.lang.reflect.Type
import java.time.LocalDate
import java.time.OffsetDateTime
import javax.ws.rs.ext.ParamConverter
import javax.ws.rs.ext.ParamConverterProvider
import javax.ws.rs.ext.Provider

@Provider
class LocalDateParamProvider: ParamConverterProvider {

    @Suppress("UNCHECKED_CAST")
    override fun <T> getConverter(
        rawType: Class<T>, genericType: Type?,
        annotations: Array<Annotation?>?
    ): ParamConverter<T>? {
        return if (rawType == LocalDate::class.java) LocalDateParamConverter() as ParamConverter<T>? else null
    }

    /**
     * ParamConverter implementation for converting LocalDate
     */
    private class LocalDateParamConverter : ParamConverter<LocalDate> {

        override fun toString(value: LocalDate?): String? {
            val datetime = value ?: return null
            return datetime.toString()
        }

        override fun fromString(value: String?): LocalDate? {
            val datetimeString = value ?: return null
            return LocalDate.parse(datetimeString)
        }
    }
}