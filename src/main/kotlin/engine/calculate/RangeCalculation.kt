package org.com.service.engine.calculate

class RangeCalculation: Calculation {



    override fun <R : Any, T> calculate(input: R, parameters: Map<String, Any?>): T {

        var result: Any = input

        val operation = parameters["rangeType"] as? String
            ?: throw IllegalArgumentException("Missing 'operation' parameter")

        val rangeInput = parameters["outerMap"] as? HashMap<String, HashMap<String, Any>>
            ?: throw IllegalArgumentException("Missing or invalid 'multiplier' parameter")

        require(input is String) {
            "Input should only contain String characters"
        }

        val resultBuffer = StringBuilder()
        val inputParam = input


        if (operation == "2bitRange") {

            // Handle a nested HashMap of HashMaps
            val hexValue = input.toIntOrNull(16)
                ?: throw IllegalArgumentException("Invalid hexadecimal input format.")

            val formattedHex = String.format("%02X", hexValue)

            @Suppress("UNCHECKED_CAST")
            val nestedMap = rangeInput as HashMap<String, HashMap<String, Any>>

            nestedMap.forEach { (key, subMap) ->
                when (key) {
                    "0x,firstBit" -> {
                        // Extract the MIL status based on the first digit
                        val firstNibble = (hexValue shr 4) and 0x0F
                        val mapKey = "0xX$firstNibble"
                        val optionLevel = subMap[firstNibble.toString()] ?: "Unknown level"
                        if(optionLevel != "Unknown level"){
                            resultBuffer.append("$optionLevel ")
                        }

                    }

                    "0x,lastBit" -> {
                        // Extract the Fuel level based on the last digit
                        val lastNibble = hexValue and 0x0F
                        val mapKey = "0xX$lastNibble"
                        val optionLevel = subMap[lastNibble.toString()] ?: "Unknown level"
                        if(optionLevel != "Unknown level"){
                            resultBuffer.append("$optionLevel ")
                        }
                    }

                    "0x,bothBit" -> {
                        // Extract the Fuel level based on the last digit
                        //val firstTwoBits = (hexValue shr 6) and 0x03
                        //val mapKey = "0xX$firstTwoBits"
                        val optionLevel = subMap[input] ?: "Unknown level"
                        if(optionLevel != "Unknown level"){
                            resultBuffer.append("$optionLevel ")
                        }
                    }

                    "0b,binary" -> {



                        // Extract the Fuel level based on the last digit
                        inputParam.reversed().forEachIndexed { index, bit ->
                            // Create the map key using the bit position
                            val isOn = bit == '1'
                            val mapKey = "${index+1}"
                            val optionLevel = subMap[mapKey] ?: "Unknown level"
                            if (optionLevel != "Unknown level") {
                                resultBuffer.append("$optionLevel: ${if (isOn) "ON" else "OFF"} ")
                            }
                        }
                    }
                }
            }

        } else if (operation == "BinaryRange") {
            require(!input.all { it == '0' || it == '1' }) {
                "Binary string can only contain 0 and 1"
            }


            when (rangeInput) {
                is HashMap<*, *> -> {
                    // Check if it's a flat HashMap or a nested HashMap
                    val isNestedMap = rangeInput.values.all { it is HashMap<*, *> }

                    if (!isNestedMap) {
                        // Handle a flat HashMap
                        rangeInput.forEach { (indexString, option) ->
                            val index = indexString.toString().toIntOrNull()
                            if (index != null && index < inputParam.length) {
                                val isOn = inputParam[index] == '1'
                                resultBuffer.append("$option: ${if (isOn) "ON" else "OFF"} ")
                            }
                        }
                    }
                }
                else -> {
                    throw IllegalArgumentException("Unsupported rangeInput type. Expected HashMap.")
                }
            }

            //resultBuffer

        }

        return resultBuffer.toString() as T

    }
}