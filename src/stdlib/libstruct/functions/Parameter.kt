package stdlib.libstruct.functions

import parser.ast.expressions.Expression
import stdlib.libstruct.BailaType

/**
 * Represents a function parameter.
 * @param name Name of the parameter (needs to be an identifier, since this is the name of the variable that the
 * argument binds to.
 * @param type Type of the parameter. Since functions are strictly typed (and only strictly typed), the parameter
 * type has to be provided.
 * @param defaultValue Default value of the parameter. If the corresponding argument is not provided, then the
 * argument variable will be assigned with the default value. It is an expression in order for the value to be
 * evaluated each time the function is called.
 * @param vararg If true, this parameter is the variadic argument parameter.
 */
data class Parameter(val name: String, val type: BailaType, val defaultValue: Expression? = null, val vararg: Boolean = false)

/**
 * Allows for easier built-in implementation of functions.
 * Format: "'...'? &lt;NAME> : &lt;TYPE>".toParameter()
 */
fun String.toParameter() : Parameter {
    val repr = trim()

    val name: String
    val type: BailaType
    val defaultValue: Expression? = null
    val vararg: Boolean = repr.startsWith("...")

    val indexOfColon = repr.indexOf(':')

    if (indexOfColon < 0) throw IllegalArgumentException("Colon is required in parameter shorthand '$repr'")

    if (repr.startsWith("...")) {
        if (indexOfColon < 4) {
            throw IllegalArgumentException("Unexpected colon in parameter shorthand '$repr'")
        }

        name = repr.substring(3, indexOfColon).trim()
        type = BailaType(repr.substring(indexOfColon + 1, repr.length).trim())
    } else {
        if (indexOfColon < 1) {
            throw IllegalArgumentException("Unexpected colon in parameter shorthand '$repr'")
        }

        name = repr.substring(0, indexOfColon).trim()
        type = BailaType(repr.substring(indexOfColon + 1, repr.length).trim())
    }

    Debug.log("name='$name', typeSubstring='${repr.substring(indexOfColon + 1, repr.length).trim()}'")

    return Parameter(name, type, defaultValue, vararg)
}