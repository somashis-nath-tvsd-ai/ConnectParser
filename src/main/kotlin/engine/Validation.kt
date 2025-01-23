package org.com.service.engine

interface Validation {

    fun<R,T> validate(input: R, validateAgainst: T): Boolean
}