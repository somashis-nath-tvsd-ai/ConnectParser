package org.com.service.engine.cache

class CacheFactory<T>(//private val context: Context? = null
 ) {
    fun getCache(): Cache<T> {
//        return if (context != null) {
//            val sharedPreferences = context.getSharedPreferences("app_cache", Context.MODE_PRIVATE)
//            SharedPreferencesCache(sharedPreferences)
//        } else {
//            CaffeineCache()
//        }

        return CaffeineCache()
    }

    // Method to create the cache based on the generic type T
    fun createCache(): Cache<T> {
        // Here we assume that CaffeineCache is the default cache implementation
        return CaffeineCache<T>()
    }

}