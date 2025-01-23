package org.com.service.engine.conversion

import org.com.service.engine.Validation

class HexadecimalToInteger: Conversion {
    override fun <T, R> convert(toConvert: T): R {

        if (toConvert !is String) {
            throw IllegalArgumentException("Input must be a hexadecimal string")
        }
        // Check if the input contains a valid hexadecimal value
        val hexRegex = Regex("^[0-9a-fA-F]+$")
        if (!hexRegex.matches(toConvert)) {
            throw IllegalArgumentException("Input must be a valid hexadecimal string")
        }

        val convertedValue = toConvert.toInt(16).toString()

        return convertedValue as R
    }




}