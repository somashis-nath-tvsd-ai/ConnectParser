package org.com.service.engine.conversion

interface Conversion {

    fun <T, R> convert(toConvert: T): R
}