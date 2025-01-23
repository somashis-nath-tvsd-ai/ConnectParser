package org.com.service.engine.ruleEngine

import org.com.service.engine.calculate.Calculation
import org.com.service.engine.conversion.Conversion

 open class Rule {

     open fun formRule(conversion: Conversion, calculate: Calculation): String{
         // Default logic here
         return "Default formRule logic"
     }


     open fun<T> generateValue(conversion: Conversion, calculate: Calculation): T{
         // Provide a meaningful default implementation or throw an exception if not implemented
         throw NotImplementedError("Default generateValue logic is not implemented")
     }

}