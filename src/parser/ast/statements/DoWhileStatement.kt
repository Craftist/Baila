package parser.ast.statements

import parser.ast.expressions.Expression
import stdlib.NameTable

class DoWhileStatement(private val condition: Expression, private val body: Statement) : Statement {
    override fun execute() {
        NameTable.pushScope()

        val condition = condition.eval()
        do {
            body.execute()
        } while (condition.asBoolean())

        NameTable.popScope()
    }

    override fun toString(): String {
        return "DoWhileStatement(condition=$condition, body=$body)"
    }
}