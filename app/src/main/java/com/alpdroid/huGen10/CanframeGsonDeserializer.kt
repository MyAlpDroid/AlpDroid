package com.alpdroid.huGen10

import android.util.Log
import com.alpdroid.huGen10.CanFrame
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import org.json.JSONArray
import java.lang.reflect.Type

class CanframeGsonDeserializer : JsonDeserializer<CanFrame?> {

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement, type: Type?,
        jsonDeserializationContext: JsonDeserializationContext?
    ): CanFrame? {
        if (json.isJsonObject) {
            val jsonObject = json.asJsonObject
            val jsoncanID = (jsonObject["id"].asString).toInt(16)
            val jsonbusID = jsonObject["bus"].asInt
            val jsonbyteArray = jsonObject.getAsJsonArray("data")
            val bytejson = ByteArray(8)
            if (jsonbyteArray.isJsonArray) {
                for (i in 0..7) {
                    if (i<jsonbyteArray.size()) {
                        bytejson[i] = ((jsonbyteArray.get(i).asString).toInt(16)).toByte()
                     }
                    else
                        bytejson[i] = 0x00
                }

                return CanFrame(
                    jsonbusID,
                    jsoncanID,
                    bytejson
                )
            }
        }
        return null // Bad Frame
    }
}