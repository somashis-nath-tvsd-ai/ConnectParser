package org.com.service.engine.cache

import com.github.benmanes.caffeine.cache.Caffeine
import java.util.concurrent.TimeUnit

class CaffeineCache<T> : Cache<T> {
    private val cache = Caffeine.newBuilder()
        .expireAfterWrite(100, TimeUnit.MINUTES)
        .maximumSize(100)
        .build<String, T>()

    override fun put(key: String, value: Any) {
        @Suppress("UNCHECKED_CAST")
        cache.put(key, value as T)
    }

    override fun get(key: String): T? {
        return cache.getIfPresent(key)
    }

    override fun clear() {
        cache.invalidateAll()
    }
}