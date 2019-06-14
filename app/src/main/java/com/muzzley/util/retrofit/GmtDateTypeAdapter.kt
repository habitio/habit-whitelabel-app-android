package com.muzzley.util.retrofit

import com.google.gson.*
import com.muzzley.Constants
import java.lang.reflect.Type
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class GmtDateTypeAdapter : JsonSerializer<Date>, JsonDeserializer<Date> {

    private val dateFormat = SimpleDateFormat(Constants.DATE_FORMAT, Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }

    @Synchronized
    override
    fun serialize(date: Date, type: Type, jsonSerializationContext: JsonSerializationContext) =
            synchronized(dateFormat) {
                JsonPrimitive(dateFormat.format(date))
            }

    @Synchronized
    override
    fun deserialize(jsonElement: JsonElement, type: Type, jsonDeserializationContext: JsonDeserializationContext) =
            synchronized(dateFormat) {
                try {
                    dateFormat.parse(jsonElement.asString)
                } catch (e: ParseException) {
//                    throw JsonSyntaxException(jsonElement.asString, e)
                    try {
                        //FIXME: Support for old wrong format. Deprecate this after 2019-08-01
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                                .apply { timeZone = TimeZone.getTimeZone("UTC") }
                                .parse(jsonElement.asString)

                    } catch (e2: ParseException) {
                        throw JsonSyntaxException(jsonElement.asString, e2)
                    }
                }
            }
}