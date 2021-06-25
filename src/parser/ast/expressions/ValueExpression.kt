package parser.ast.expressions

import stdlib.values.Value

class ValueExpression(private val value:Value) : Expression {
    override fun eval(): Value {
        return value
    }

    override fun toString(): String {
        return "ValueExpression($value)"
    }

    override fun asCodeRepr(): String = "$value"
}