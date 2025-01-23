package org.com.service.engine.execution

import org.json.JSONArray
import org.json.JSONObject

class JsonTransformer {

    fun transformJson(inputJson: JSONObject): JSONObject {
        val outputJson = JSONObject()

        // Map main attributes
        outputJson.put("uniqueId", "vehicle_data_001")
        outputJson.put("name", inputJson.getString("name"))
        outputJson.put("description", "JSON structure for vehicle telemetry data including metrics such as speed, odometer value, fuel level, and more.")

        val transformedData = JSONObject()
        val inputDataArray = inputJson.getJSONArray("data")

        for (i in 0 until inputDataArray.length()) {
            val dataEntry = inputDataArray.getJSONObject(i)
            val dataName = dataEntry.getString("data_name")

            val transformedEntry = JSONObject()

            // Map index_number to index
            transformedEntry.put("index", dataEntry.getInt("index_number"))

            // Map prefix
            transformedEntry.put("prefix", dataEntry.optString("prefix", "none"))

            // Map transfer_type to conversion
            val transferTypes = dataEntry.getJSONArray("transfer_type")
            val conversion = when {
                transferTypes.contains("Integer") -> "HexadecimalToInteger"
                transferTypes.contains("Binary") -> "HexadecimalToBinary"
                else -> "none"
            }
            transformedEntry.put("conversion", conversion)

            // Map range_type to RangeCalculation if present
            val rangeTypes = dataEntry.optJSONArray("range_type")
            if (rangeTypes != null) {
                transformedEntry.put("calculation", "RangeCalculation")
                transformedEntry.put("rangeType", "2bitRange")

                val ranges = JSONArray()
                for (j in 0 until rangeTypes.length()) {
                    val rangeTypeEntry = rangeTypes.getJSONObject(j)
                    val rangeObject = JSONObject()

                    rangeObject.put("name", rangeTypeEntry.getString("range_scope"))

                    val rangeScope = rangeTypeEntry.getString("range_scope")
                    rangeObject.put("type", when (rangeScope) {
                        "binary" -> "0b,binary"
                        "bitwise" -> "0x,firstBit"
                        "hexadecimal" -> "0x,lastBit"
                        else -> "unknown"
                    })

                    val rangeMap = rangeTypeEntry.getJSONObject("range_map")
                    rangeObject.put("values", rangeMap)

                    ranges.put(rangeObject)
                }
                transformedEntry.put("range", ranges)
            } else {
                transformedEntry.put("calculation", "none")
            }

            // Map suffix
            transformedEntry.put("suffix", dataEntry.optString("suffix", "none"))

            // Add transformed entry to output data
            transformedData.put(dataName, transformedEntry)
        }

        outputJson.put("data", transformedData)
        return outputJson
    }

    fun JSONArray.contains(value: String): Boolean {
        for (i in 0 until this.length()) {
            if (this.getString(i) == value) return true
        }
        return false
    }

}