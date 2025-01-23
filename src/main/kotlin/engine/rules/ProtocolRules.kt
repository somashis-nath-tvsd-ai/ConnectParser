package org.com.service.engine.rules

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.com.service.engine.cache.Cache
import org.com.service.engine.cache.CacheFactory
import org.com.service.engine.cache.CacheManager
import org.com.service.engine.calculate.ConfigurableCalculationFactory
import org.com.service.engine.conversion.ConfigurableConversionFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.ObjectOutputStream

class ProtocolRules:Rules {

    override fun captureRules() {
        // Load JSON file from the resource folder
        val classLoader = Thread.currentThread().contextClassLoader
        val jsonFile = File(classLoader.getResource("Rules.json")!!.file)

        // Parse JSON into a JsonNode structure
        val objectMapper = ObjectMapper()
        val rootNode: JsonNode = objectMapper.readTree(jsonFile)

        // Resulting LinkedHashMap to hold the final structure
        val resultMap = LinkedHashMap<String, LinkedHashMap<String, Any>>()

        //val startTime = System.currentTimeMillis()

        if (rootNode.isArray) {
            rootNode.forEach { jsonObject ->
                val name = jsonObject["name"].asText() // Top-level key
                val dataNode = jsonObject["data"] // Get the "data" field

                // Convert "data" to LinkedHashMap and map byte values
                //val dataMap = convertDataNodeToOrderedMapWithBytes(dataNode, byteArray)

                val dataMap = convertDataNodeToOrderedMapWithBytes(dataNode)

                val rulesCache = CacheManager.getCache<LinkedHashMap<String, Any>>(name)
                if (rulesCache != null) {
                    rulesCache.put(name, dataMap)
                }

                //val gson = Gson()
                //val jsonString = gson.toJson(Parser().execute(dataMap))
                //println(jsonString)

                // Add to resultMap with the name as the key
                //resultMap[name] = dataMap

                println("System Cache Contents:")

            }
        }
    }

    fun captureConfig(){
        // Load the JSON file from the resources folder
        val classLoader = Thread.currentThread().contextClassLoader
        val jsonFile = File(classLoader.getResource("Config.json")!!.file)

        // Parse JSON into a JsonNode structure
        val objectMapper = ObjectMapper()
        val rootNode: JsonNode = objectMapper.readTree(jsonFile)

        // Extract the 'properties' node (which is an array in your case)
        val propertiesNode = rootNode.path("properties").first()

        // Create a map to store class details
        val classMap = mutableMapOf<String, HashMap<String, String>>()

        // Iterate through each entry in 'properties'
        propertiesNode.fields().forEachRemaining { (key, value) ->
            // For each key, we need to extract the 'className' and other properties like 'parameter'
            val classDetails = hashMapOf<String, String>()

            // Get the 'className' value
            classDetails["className"] = value.path("className").asText()
            classDetails["class type"] = value.path("class type").asText()

            // Get the 'parameter' value as a comma-separated list
            val parametersNode = value.path("parameter")
            if (parametersNode.isArray) {
                val parameters = parametersNode.joinToString(",") { it.asText() }
                classDetails["parameter"] = parameters
            }

            // Put the class details in the map with the key as the class name (e.g., RangeCalculation)
            classMap[key] = classDetails
        }

        // Now add the classMap to the cache
        val name = "properties"  // The name of the cache to store under
        val rulesCache = CacheManager.getCache<LinkedHashMap<String, Any>>(name)

        // If the cache is not null, put the classMap data into it
        if (rulesCache != null) {
            rulesCache.put(name, classMap)
            println("\nData added to cache under the name '$name'.")
        } else {
            println("\nCache is not available.")
        }

    }

    fun loadInstances(){

        val fetchedConfigCache = CacheManager.getCache<LinkedHashMap<String, Any>>("properties")
        //println("Another Addition Cache: ${fetchedConfigCache?.get("properties")}")
        val configMap = fetchedConfigCache?.get("properties")

        var instanceCache: Cache<MutableMap<String,Any>>? = null
        instanceCache = CacheManager.getCache("instances")

        if (configMap != null) {
            val classDetails = hashMapOf<String, Any>()
            for ((key, value) in configMap) {

                if (value is HashMap<*, *>) {
                    val classType = value["class type"]
                    val classname = value["className"]
                    if(classType == "Conversion"){
                        var instance = ConfigurableConversionFactory().createConversion(classname.toString())
                        if (classDetails != null) {
                            classDetails.put(key,instance)
                        }
                    }
                    if(classType == "Calculation"){
                        var instance = ConfigurableCalculationFactory().createCalculation(classname.toString())
                        if (classDetails != null) {
                            classDetails.put(key,instance)
                        }
                    }

                    println("Key: $key, Value: $value")
                }
            }

            instanceCache?.put("instances",classDetails)
        }


    }

    fun convertToByteArrayUsingObjectStream(any: Any): ByteArray? {
        return try {
            ByteArrayOutputStream().use { byteArrayOutputStream ->
                ObjectOutputStream(byteArrayOutputStream).use { objectOutputStream ->
                    objectOutputStream.writeObject(any)
                    objectOutputStream.flush()
                    byteArrayOutputStream.toByteArray()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    // Function to process the "data" field and map byte values to LinkedHashMap
    @OptIn(ExperimentalStdlibApi::class)
    fun convertDataNodeToOrderedMapWithBytes(dataNode: JsonNode): LinkedHashMap<String, Any> {
        val dataMap = LinkedHashMap<String, Any>()

        val conversionSet = mutableSetOf<String>()
        val calculationSet = mutableSetOf<String>()
        val rangeTypeSet = mutableSetOf<String>()
        val operationTypeSet = mutableSetOf<String>()

        if (dataNode.isArray) {
            dataNode.forEach { dataObject ->
                if (dataObject.isObject) {
                    dataObject.fields().forEachRemaining { (key, value) ->
                        // Convert the JSON node to a map
                        val fieldMap = jsonToOrderedMap(value) as LinkedHashMap<String, Any>

                        // If the field has an "index", map the byte value
                        if (fieldMap.containsKey("index")) {
                            val indexString = fieldMap["index"].toString()
                            val indices = indexString.split(",").map { it.trim().toInt() - 1 } // Convert to zero-based index
                            //val byteValue = indices.joinToString("") { byteArray[it].toHexString(HexFormat.UpperCase) }

                            val conversion = fieldMap["conversion"]?.toString()?.takeIf { it.isNotBlank() && it != "none" }
                            addIfValid(conversion.toString(),conversionSet)
                            val calculation = fieldMap["calculation"]?.toString()?.takeIf { it.isNotBlank() && it != "none" }
                            addIfValid(calculation.toString(),calculationSet)
                            val rangeType = fieldMap["rangeType"]?.toString()?.takeIf { it.isNotBlank() && it != "none" }
                            addIfValid(rangeType.toString(),rangeTypeSet)
                            //val operationType = fieldMap["formula"]?.toString()?.takeIf { it.isNotBlank() && it != "none" } != null
                            //val operator = operationType.toString().split(",")[0].single()
                            //addIfValid(operator.toString(),operationTypeSet)

                            // Add the byte value to the map
                            //fieldMap["byte_value"] = byteValue
                        }

                        // Add the field map to the main data map
                        dataMap[key] = fieldMap
                    }
                }
            }
        }

        arrangeCacheData("conversion",conversionSet)
        arrangeCacheData("calculation",calculationSet)
        arrangeCacheData("rangeType",rangeTypeSet)


        return dataMap
    }

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

    fun arrangeCacheData(name: String, dataList: MutableSet<String>) {

        var workingCache: Cache<MutableSet<String>>? = null

        // Select the appropriate cache based on the name
        when (name) {
            "conversion" -> workingCache = CacheManager.getCache("conversion")
            "calculation" -> workingCache = CacheManager.getCache("calculation")
            "range" -> workingCache = CacheManager.getCache("range")
        }

        // Fetch the cache instance
        val existingData = workingCache?.get(name)

// Update the cache with new data
        if (existingData != null) {
            // Add new values to the existing set
            existingData.addAll(dataList)
            if (workingCache != null) {
                workingCache.put(name, existingData)
            }
        } else {
            // Create a new set and add data to the cache
            workingCache?.put(name, dataList)
        }

    }

    fun addIfValid(entry: String?,validSet: MutableSet<String> ) {
        entry?.takeIf { it.isNotBlank() && it != "null" }?.let { validSet.add(it) }
    }
}