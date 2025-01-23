package org.com.service.engine.conversion


interface ConversionFactory {

    fun createConversion(className: String): Conversion

}