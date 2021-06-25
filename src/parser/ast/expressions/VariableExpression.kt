package parser.ast.expressions

import stdlib.NameTable
import stdlib.values.Value

class VariableExpression(val name: String) : Expression {
    override fun eval(): Value {
        return NameTable.get(name).value
    }

    override fun toString(): String {
        return "VariableExpression($name)"
    }

    override fun asCodeRepr(): String = "$name"
}