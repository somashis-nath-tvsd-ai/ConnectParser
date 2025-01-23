package org.com.service.engine.calculate

interface CalculationFactory {

    fun createCalculation(operation: String): Calculation

}