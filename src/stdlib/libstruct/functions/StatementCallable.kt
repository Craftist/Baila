package stdlib.libstruct.functions

import parser.ast.exceptions.ReturnException
import parser.ast.statements.BlockStatement
import parser.ast.statements.ExpressionStatement
import parser.ast.statements.Statement
import stdlib.values.Value

/**
 * Allows for statements to be executed as a callable.
 * Used primarily for blocks of code.
 */
class StatementCallable(private val stmt: Statement) : Callable() {
    override fun call(args: Array<Value>): Value? {
        try {
            stmt.execute()

            if (stmt is BlockStatement) {
                var lastStmt = stmt.statements.last();
                if (lastStmt is ExpressionStatement) {
                    return lastStmt.evaluated
                }
            }
        } catch (e: ReturnException) {
            return e.expr?.eval() // return encountered
        }

        return null // kinda like void
    }
}