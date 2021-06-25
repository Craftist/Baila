package parser.ast.statements

import parser.ast.expressions.Expression
import stdlib.values.Value

class ExpressionStatement(val expr: Expression) : Statement {
    var evaluated: Value? = null

    override fun execute() {
        val ev = expr.eval()
        if (evaluated == null) {
            evaluated = ev
        }
    }

    override fun toString(): String {
        return "ExpressionStatement(expr=$expr)"
    }
}