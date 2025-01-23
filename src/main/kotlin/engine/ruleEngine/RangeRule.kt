package org.com.service.engine.ruleEngine

import org.com.service.engine.calculate.Calculation
import org.com.service.engine.conversion.Conversion

class RangeRule: Rule() {

    override fun formRule(conversion: Conversion, calculate: Calculation): String{

        val converseionNeeded = conversion
        val calculationNeeded = calculate

        if(calculate != null && conversion != null){

        }

        // Default logic here
        if(conversion == null){

        }

        if(calculate == null){

        }



        return "Default formRule logic"
    }

}