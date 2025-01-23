package org.com.service.engine.conversion

class HexadecimalToBinary: Conversion {
    override fun <T, R> convert(toConvert: T): R {

        if (toConvert !is String) {
            throw IllegalArgumentException("Input must be a hexadecimal string")
        }

        val hexRegex = Regex("^[0-9a-fA-F]+$")
        if (!hexRegex.matches(toConvert)) {
            throw IllegalArgumentException("Input must be a valid hexadecimal string")
        }

        val intValue = toConvert.removePrefix("0x").toIntOrNull(16)
            ?: throw IllegalArgumentException("Invalid hexadecimal input")

        val binaryString = intValue.toString(2)

        return binaryString as R

    }
}