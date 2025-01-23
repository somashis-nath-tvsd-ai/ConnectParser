package org.com.service.engine.calculate

import org.com.service.engine.cache.CacheManager
import kotlin.reflect.full.primaryConstructor

// Concrete factory class that is configurable via JSON
class ConfigurableCalculationFactory() : CalculationFactory {
    // Create the Calculation instance using class name from the cache
    override fun createCalculation(className: String): Calculation {
        // Retrieve the set of class names from the cache for the given operation
        //val existingData = CacheManager.getCache<MutableSet<String>>("calculation")?.get(operation)

//        if (existingData.isNullOrEmpty()) {
//            throw IllegalArgumentException("Operation not supported or class names not found for operation: $operation")
//        }
//
//        // For simplicity, we just pick the first class name for the given operation
//        val className = existingData.first()
        try {
            // Use reflection to instantiate the Calculation class dynamically
            val clazz = Class.forName(className).kotlin
            return clazz.primaryConstructor?.call() as Calculation
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to instantiate Calculation for operation: $className", e)
        }
    }
}