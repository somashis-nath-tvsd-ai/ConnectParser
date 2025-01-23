package org.com.service

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import org.com.service.engine.execution.Parser
import org.com.service.engine.rules.ProtocolRules
import java.io.File

fun main() {
    // Byte array input



    val byteArray = byteArrayOf(
        0x5A.toByte(), 0x10.toByte(), 0x64.toByte(), 0x11.toByte(), 0x86.toByte(), 0x9F.toByte(),
        0x06.toByte(), 0x64.toByte(), 0x00.toByte(), 0x64.toByte(), 0x96.toByte(), 0x51.toByte(),
        0x01.toByte(), 0x02.toByte(), 0x00.toByte(), 0x00.toByte(), 0x78.toByte(), 0x2E.toByte(), 0xE0.toByte(), 0xFF.toByte()
    )

    val secondByteArray = byteArrayOf(
        0x5A.toByte(), 0x11.toByte(), 0x01.toByte(), 0x5A.toByte(), 0x05.toByte(), 0x5A.toByte(),
        0x00.toByte(), 0x0A.toByte(), 0x00.toByte(), 0x01.toByte(), 0x5A.toByte(), 0x00.toByte(),
        0x01.toByte(), 0xC8.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x32.toByte(), 0xFF.toByte()
    )

    // one time execution at the start of the app
    ProtocolRules().captureRules()
    ProtocolRules().captureConfig()
    ProtocolRules().loadInstances()

    // one time execution completes

    val startTime = System.currentTimeMillis()

    val gson = Gson()
    val jsonString = gson.toJson(Parser().execute(byteArray,"Speedometer Data - 1 /NTORQ"))
    println(jsonString)

    //println("Next Element")

//    val jsonNextString = gson.toJson(Parser().execute(secondByteArray,"Speedometer Data - 2"))
//    println(jsonNextString)

    val endTime = System.currentTimeMillis()
    val executionTime = endTime - startTime
    println("Execution Time: $executionTime milliseconds")


}

// Function to process the "data" field and map byte values to LinkedHashMap
//@OptIn(ExperimentalStdlibApi::class)
//fun convertDataNodeToOrderedMapWithBytes(dataNode: JsonNode, byteArray: ByteArray): LinkedHashMap<String, Any> {
//    val dataMap = LinkedHashMap<String, Any>()
//
//    if (dataNode.isArray) {
//        dataNode.forEach { dataObject ->
//            if (dataObject.isObject) {
//                dataObject.fields().forEachRemaining { (key, value) ->
//                    // Convert the JSON node to a map
//                    val fieldMap = jsonToOrderedMap(value) as LinkedHashMap<String, Any>
//
//                    // If the field has an "index", map the byte value
//                    if (fieldMap.containsKey("index")) {
//                        val indexString = fieldMap["index"].toString()
//                        val indices = indexString.split(",").map { it.trim().toInt() - 1 } // Convert to zero-based index
//                        val byteValue = indices.joinToString("") { byteArray[it].toHexString(HexFormat.UpperCase) }
//
//                        // Add the byte value to the map
//                        fieldMap["byte_value"] = byteValue
//                    }
//
//                    // Add the field map to the main data map
//                    dataMap[key] = fieldMap
//                }
//            }
//        }
//    }
//    return dataMap
//}

// Recursive function to convert JSON to LinkedHashMap
fun jsonToOrderedMap(jsonNode: JsonNode): Any {
    return when {
        jsonNode.isObject -> {
            val childMap = LinkedHashMap<String, Any>()
            jsonNode.fields().forEachRemaining { (key, value) ->
                childMap[key] = jsonToOrderedMap(value)
            }
            childMap
        }
        jsonNode.isArray -> {
            val arrayList = mutableListOf<Any>()
            jsonNode.forEach { childNode ->
                arrayList.add(jsonToOrderedMap(childNode))
            }
            arrayList
        }
        else -> jsonNode.asText()
    }
}
