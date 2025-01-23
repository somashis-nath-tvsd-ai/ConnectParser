package org.com.service.engine.conversion

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.com.service.engine.cache.Cache
import org.com.service.engine.cache.CacheFactory
import org.com.service.engine.cache.CacheManager
import org.com.service.engine.calculate.Calculation
import kotlin.reflect.full.primaryConstructor

class ConfigurableConversionFactory() : ConversionFactory {

    // Create the Calculation instance using class name from the cache
    override fun createConversion(className: String): Conversion {
        // Retrieve the set of class names from the cache for the given operation
        //val existingData = CacheManager.getCache<MutableSet<String>>(name)?.get(operation)

        //if (existingData.isNullOrEmpty()) {
        //    throw IllegalArgumentException("Operation not supported or class names not found for operation: $operation")
        //}

        // For simplicity, we just pick the first class name for the given operation
        //val className = "org.com.service.engine.conversion."+operation
        //val modifiedClassname = className.replace(Regex("[{}\\[\\]()]"), "")



        try {

            // Use reflection to instantiate the Calculation class dynamically
            val clazz = Class.forName(className).kotlin
            val primaryConstructorCall = clazz.primaryConstructor?.call()
            val conversion = primaryConstructorCall as Conversion

            return conversion


        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to instantiate Calculation for operation: $className", e)
        }


    }

}