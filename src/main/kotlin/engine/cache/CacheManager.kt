package org.com.service.engine.cache

// CacheManager: Singleton that manages multiple caches of different types
object CacheManager {
    // A map to hold caches of different types
    private val cacheMap = mutableMapOf<String, Cache<Any>>()

    // Check if cache exists and return it or create a new one
    fun <T> getCache(name: String): Cache<T>? {
        // Check if cache already exists
        if (cacheMap.containsKey(name)) {
            return cacheMap[name] as? Cache<T>
        }

        // If cache does not exist, create a new one
        val cache = CacheFactory<T>().createCache()
        cacheMap[name] = cache as Cache<Any>
        return cache
    }

    // Add a cache to the cache manager
    fun <T> addCache(name: String, cache: Cache<T>) {
        cacheMap[name] = cache as Cache<Any>
    }

    // Optional: clear all caches
    fun clearAll() {
        cacheMap.clear()
    }
}
