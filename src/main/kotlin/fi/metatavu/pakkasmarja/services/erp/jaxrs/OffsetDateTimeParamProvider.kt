package fi.metatavu.pakkasmarja.services.erp.jaxrs

import java.lang.reflect.Type
import javax.ws.rs.ext.ParamConverter
import javax.ws.rs.ext.ParamConverterProvider
import java.time.OffsetDateTime
import javax.ws.rs.ext.Provider

/**
 * JAX-RS parameter converter for OffsetDateTime
 */
@Provider
class OffsetDateTimeParamProvider : ParamConverterProvider {

    @Suppress("UNCHECKED_CAST")
    override fun <T> getConverter(
        rawType: Class<T>, genericType: Type?,
        annotations: Array<Annotation?>?
    ): ParamConverter<T>? {
        return if (rawType == OffsetDateTime::class.java) OffsetDateTimeParamConverter() as ParamConverter<T>? else null
    }

    /**
     * ParamConverter implementation for converting OffsetDateTime
     */
    private class OffsetDateTimeParamConverter : ParamConverter<OffsetDateTime> {

        override fun toString(value: OffsetDateTime?): String? {
            val datetime = value ?: return null
            return datetime.toString()
        }

        override fun fromString(value: String?): OffsetDateTime? {
            val datetimeString = value ?: return null
            return OffsetDateTime.parse(datetimeString)
        }
    }
}
