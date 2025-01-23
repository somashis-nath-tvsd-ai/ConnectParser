package org.com.service.engine.calculate

interface Calculation {

    @Suppress("UNCHECKED_CAST")
    fun <R : Any, T> calculate(input: R, parameters: Map<String, Any?>): T

}