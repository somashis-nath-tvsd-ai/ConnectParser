package org.com.service.engine.calculate

class BasicCalculation: Calculation {
    @Suppress("UNCHECKED_CAST")
    override fun <R : Any, T> calculate(input: R, parameters: Map<String, Any?>): T {
        // Initial result is set to the input
        var result: Any = input

        val operation = parameters["formula"] as? String
            ?: throw IllegalArgumentException("Missing 'operation' parameter")

        val suffix = parameters["suffix"] as? String
            ?: throw IllegalArgumentException("Missing 'suffix' parameter")

        // If no operation is provided
        if (operation.isEmpty()) {
            result = "$result$suffix"
        } else {
            // Extract operator and operand from the operation string
            val operator = operation.split(",")[0].single()
            val operand = operation.split(",")[1].toDoubleOrNull()
                ?: throw IllegalArgumentException("Invalid operand in operation: $operation")

            // Convert input to Double for calculation
            val inputValue: Double = when (input) {
                is Int -> input.toDouble()
                is String -> input.toDoubleOrNull()
                    ?: throw IllegalArgumentException("Input must be a valid number string: $input")
                is Double -> input
                else -> throw IllegalArgumentException("Unsupported input type: ${input::class}")
            }

            // Perform the calculation
            val calculatedValue = calculateFormula(inputValue, operand, operator)
            result = calculatedValue.toString()
        }

        // Cast the result to the required return type
        return result as T
    }

    fun calculateFormula(operand1: Double, operand2: Double, operator: Char): Double {
        return when (operator) {
            '+' -> operand1 + operand2
            '-' -> operand1 - operand2
            '*' -> operand1 * operand2
            '/' -> {
                if (operand2 == 0.0) throw IllegalArgumentException("Division by zero is not allowed")
                operand1 / operand2
            }
            else -> throw IllegalArgumentException("Unknown operator: $operator")
        }
    }

}