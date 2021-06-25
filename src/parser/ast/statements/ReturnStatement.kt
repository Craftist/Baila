package parser.ast.statements

import parser.ast.exceptions.ReturnException
import parser.ast.expressions.Expression

class ReturnStatement(val expr: Expression? = null) : Statement {
    override fun execute() {
        throw ReturnException(expr)
    }

    override fun toString(): String {
        return "ReturnStatement(expr=$expr)"
    }
}