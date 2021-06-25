package parser.ast.expressions

import stdlib.values.Value

interface Expression {
    fun eval() : Value

    /**
     * Used for retrieving the type of the expression without evaluating it.
     */
    //fun getType() : Type

    /**
     * Returns the code representation of an expression
     */
    fun asCodeRepr() : String
}