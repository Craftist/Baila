package parser.ast.statements

import parser.ast.expressions.Expression
import stdlib.NameTable

class ConstantDefineStatement(private val name: String, val value: Expression) : Statement {
    override fun execute() {
        NameTable.addConstant(name, value.eval())
    }

    override fun toString(): String {
        return "ConstantDefineStatement(name=$name, value=$value)"
    }
}