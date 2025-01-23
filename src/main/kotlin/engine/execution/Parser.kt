package org.com.service.engine.execution

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext
import org.com.service.engine.cache.CacheManager
import org.com.service.engine.calculate.Calculation
import org.com.service.engine.calculate.ConfigurableCalculationFactory
import org.com.service.engine.conversion.ConfigurableConversionFactory
import org.com.service.engine.conversion.Conversion
import java.io.StringReader


open class Parser {


    fun execute(
        byteArray: ByteArray,
        name: String,
        context: CoroutineContext = Dispatchers.Default
    ): JsonObject {
        return runBlocking(context) {
            // Use a thread-safe collection to store results
            val protocolJson = JsonObject()


            // You can check that cache is not created again by calling getCache on "addition"
            val fetchedRulesCache = CacheManager.getCache<LinkedHashMap<String, Any>>(name)
            //println("Another Addition Cache: ${fetchedRulesCache?.get(name)}")
            val inputMap = fetchedRulesCache?.get(name)

            val dataMap = inputMap?.let { convertDataMapToOrderedMapWithBytes(it, byteArray) }

            // Launch coroutines for each entry in the map
            val deferredResults = inputMap?.map { (key, value) ->
                async {
                    // Safely cast value and process
                    val parsedValue = if (value is LinkedHashMap<*, *>) {
                        val parsedValue = parse(key, value as LinkedHashMap<String, Any>)
                        val jsonElement = parseLenientJson(parsedValue)
                        if (jsonElement.isJsonObject) {
                            protocolJson.add(key, jsonElement.asJsonObject)
                        } else {
                            protocolJson.addProperty(key, parsedValue)
                        }

                    } else {
                        "Invalid value for key: $key"
                    }
                }
            }

            // Await all results to ensure all coroutines are completed
            if (deferredResults != null) {
                deferredResults.awaitAll()
            }



            println(protocolJson)
            // Return the thread-safe collection
            protocolJson
        }
    }

    // Helper function to parse lenient JSON
    fun parseLenientJson(jsonString: String): com.google.gson.JsonElement {
        return try {
            JsonParser.parseString(jsonString)
        } catch (e: JsonSyntaxException) {
            // Use lenient parsing
            val reader = com.google.gson.stream.JsonReader(StringReader(jsonString))
            reader.isLenient = true
            com.google.gson.JsonParser.parseReader(reader)
        }
    }

    // Parse method to process each key's values and return a string
    suspend fun parse(mapKey: String, mapValue: LinkedHashMap<String, Any>): String {
        val resultBuilder = StringBuilder()



        // Trying to fetch the subtraction cache again
        val fetchedConversionCache = CacheManager.getCache<MutableSet<String>>("conversion")!!
        var conversionSet = fetchedConversionCache.get("conversion")

        // You can check that cache is not created again by calling getCache on "addition"
        val fetchedCalculationCache = CacheManager.getCache<MutableSet<String>>("calculation")
        var calculationSet =  fetchedCalculationCache?.get("calculation")

        val fetchedConfigCache = CacheManager.getCache<LinkedHashMap<String, Any>>("properties")
        //println("Another Addition Cache: ${fetchedConfigCache?.get("properties")}")
        val configMap = fetchedConfigCache?.get("properties")

        val fetchedInstanceCache = CacheManager.getCache<MutableMap<String, Any>>("instances")
        //println("Another Addition Cache: ${fetchedConfigCache?.get("properties")}")
        val instanceMap = fetchedInstanceCache?.get("instances")


            if (mapValue is LinkedHashMap<*, *>) {

                val varibaleMap = mutableMapOf<String, Any>()

                // Process the value map fields
                val index = mapValue["index"] ?: "N/A"
                val byteValue = mapValue["byte_value"]?.toString() ?: "N/A"
                val conversion = mapValue["conversion"]?.toString() ?: "N/A"
                val calculation = mapValue["calculation"]?.toString() ?: "N/A"
                val formula = mapValue["formula"]?.toString() ?: "N/A"
                varibaleMap.put("formula",formula)
                val suffix = mapValue["suffix"]?.toString() ?: "N/A"
                varibaleMap.put("suffix",suffix)
                val range = mapValue["range"]
                val rangeType = mapValue["rangeType"]?.toString() ?: "N/A"
                varibaleMap.put("rangeType",rangeType)
                val outerMap: MutableMap<String, LinkedHashMap<String, Any?>> = mutableMapOf()
                if (range is List<*>) {
                    println("Range contains a list of JSON objects:")
                    range.forEachIndexed { idx, item ->
                        if (item is LinkedHashMap<*, *>) {
                            println("  Range Item $idx:")
                            var type = ""
                            var name = ""
                            val updatedNestedMap = LinkedHashMap<String, Any?>()

                            item.forEach { (nestedKey, nestedValue) ->
                                when (nestedKey) {
                                    "name" -> name = nestedValue?.toString() ?: ""
                                    "type" -> type = nestedValue?.toString() ?: ""
                                    "values" -> if (nestedValue is LinkedHashMap<*, *>) {
                                        nestedValue.forEach { (innerKey, innerValue) ->
                                            updatedNestedMap[innerKey.toString()] = name + innerValue
                                        }
                                    }
                                }
                                println("    Key: $nestedKey, Value: $nestedValue")
                            }

                            if (type.isNotEmpty()) {
                                outerMap[type] = updatedNestedMap
                            } else {
                                println("    Warning: Missing 'type' key for item at index $idx")
                            }
                        } else {
                            println("  Range Item $idx is not a JSON object.")
                        }
                    }

                }
                varibaleMap.put("outerMap",outerMap)
                //val rangeCal = mapValue["range"]?.

                lateinit var conversionInstance: Conversion
                var conversionRes: String = byteValue



                if (conversionSet != null) {
                    if(conversion in conversionSet){
                        if (configMap != null) {
                            if(conversion in configMap) {
                                val conversionData = configMap.get(conversion)
                                if (conversionData != null && conversionData is HashMap<*, *>) {
                                    val convClassName = conversionData["className"]
                                    if (conversion != null) {
                                        val instanceConv = instanceMap?.get(conversion) as Conversion
                                        //conversionInstance = ConfigurableConversionFactory().createConversion(convClassName.toString())
                                        conversionRes = instanceConv.convert<String, String>(byteValue)
                                    }
                                }


                            }
                        }


                    }
                }

                lateinit var calculationInstance: Calculation
                var calculationRes: String = ""

                if (calculationSet != null) {
                    if(calculation in calculationSet){
                        val calculationFactory = ConfigurableCalculationFactory()
                        if (configMap != null) {
                            if (calculation in configMap) {
                                var calculationData = configMap.get(calculation)
                                if (calculationData != null && calculationData is HashMap<*, *>){
                                    val calcParam = calculationData["parameter"]
                                    val calcClassName = calculationData["className"]
                                    val paramMap = calcParam.toString().split(",")
                                        .associateWith { key -> varibaleMap[key] }
                                        .filterValues { it != null }

                                    if (calculation != null){
                                        val instanceCalc = instanceMap?.get(calculation) as Calculation
                                        //calculationInstance = calculationFactory.createCalculation(calcClassName.toString())
                                        calculationRes = instanceCalc.calculate<String,String>(conversionRes,paramMap)
                                    }
                                }

                            }
                        }

                    }
                }



//                val conversionResult = when (conversion) {
//                    "HexadecimalToInteger" -> {
//                        try {
//                            HexadecimalToInteger().convert<String,String>(byteValue)
//                            //Integer.parseInt(byteValue, 16).toString()
//                        } catch (e: NumberFormatException) {
//                            "Invalid Hexadecimal Value"
//                        }
//                    }
//                    "HexadecimalToBinary" -> {
//                        try {
//                            HexadecimalToBinary().convert<String,String>(byteValue)
//
////                            String.format(
////                                "%8s",
////                                Integer.toBinaryString(byteValue.toInt(16))
////                            ).replace(" ", "0")
//                        } catch (e: NumberFormatException) {
//                            "Invalid Hexadecimal Value"
//                        }
//                    }
//                    else -> "" // Default to raw byteValue if no conversion is applied
//                }
//
//                // Handle Calculation
//                val calculationResult = when (calculation) {
//                    "BasicCalculation" -> {
//                        try {
//
//                            BasicCalculation().calculate<String,String>(
//                                conversionResult,
//                                formula,
//                                suffix
//                            ).toString()
//
//                        } catch (e: NumberFormatException) {
//                            "Calculation Error"
//                        }
//                    }
//                    "RangeCalculation" -> {
//                        try {
//                        RangeCalculation().calculate<String,String>(
//                            byteValue,
//                            rangeType,
//                            outerMap
//                        ).toString()
//                        } catch (e: IllegalArgumentException) {
//                            "Range Error"
//                        }
//                    }
//                    else -> "" // If no calculation, return the converted value
//                }


                // Append results to resultBuilder
                resultBuilder.append(
                    buildString {
                        val parts = mutableListOf<String>()

                        if (!calculationRes.isNullOrEmpty() && calculationRes.lowercase() != "none") {
                            parts.add(calculationRes)
                            conversionRes.drop(conversionRes.length)
                        }

                        if (!conversionRes.isNullOrEmpty() && conversionRes.lowercase() != "none") {
                            //if(conversionRes.isNullOrEmpty())
                            parts.add(conversionRes)
                        }

                        if (!suffix.isNullOrEmpty() && suffix.lowercase() != "none") {
                            parts.add(suffix)
                        }

                        if (parts.isEmpty()) {
                            append("0x$byteValue") // Add default value if all are empty or "none"
                        } else {
                            append(parts.joinToString(" ")) // Join the valid parts with a space
                        }
                    }
                )



            }



        println(resultBuilder.toString())
        return resultBuilder.toString()
    }



    @OptIn(ExperimentalStdlibApi::class)
    fun convertDataMapToOrderedMapWithBytes(dataMap: LinkedHashMap<String, Any>, byteArray: ByteArray): LinkedHashMap<String, Any> {
        val resultDataMap = LinkedHashMap<String, Any>()

        dataMap.forEach { (key, value) ->
            if (value is LinkedHashMap<*, *>) {
                // Cast to LinkedHashMap<String, Any>
                @Suppress("UNCHECKED_CAST")
                val fieldMap = value as LinkedHashMap<String, Any>

                // If the field has an "index", map the byte value
                if (fieldMap.containsKey("index")) {
                    val indexString = fieldMap["index"].toString()
                    val indices = indexString.split(",").map { it.trim().toInt() - 1 } // Convert to zero-based index
                    val byteValue = indices.joinToString("") { byteArray[it].toHexString(HexFormat.UpperCase) }

                    // Add the byte value to the map
                    fieldMap["byte_value"] = byteValue
                }

                // Add the field map to the main result map
                resultDataMap[key] = fieldMap
            } else if (value is List<*>) {
                // Handle nested lists, assuming they contain LinkedHashMaps
                val processedList = value.mapNotNull { item ->
                    if (item is LinkedHashMap<*, *>) {
                        @Suppress("UNCHECKED_CAST")
                        convertDataMapToOrderedMapWithBytes(item as LinkedHashMap<String, Any>, byteArray)
                    } else null
                }
                resultDataMap[key] = processedList
            } else {
                // Directly add non-map values
                resultDataMap[key] = value
            }
        }

        return resultDataMap
    }


}