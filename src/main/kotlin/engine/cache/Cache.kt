package org.com.service.engine.cache

interface Cache<T> {

    fun put(key: String, value: Any)
    fun get(key: String): T?
    fun clear()

}