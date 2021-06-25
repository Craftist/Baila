package parser.ast.statements

import parser.ast.expressions.Expression
import stdlib.NameTable

class WhileStatement(private val condition: Expression, private val body: Statement) : Statement {
    override fun execute() {
        NameTable.pushScope()

        val condition = condition.eval()
        while (condition.asBoolean()) {
            body.execute()
        }

        NameTable.popScope()
    }

    override fun toString(): String {
        return "WhileStatement(condition=$condition, body=$body)"
    }
}