package parser.ast.statements

import parser.ast.expressions.Expression

class IfElseStatement(private val cond: Expression, private val trueExpr: Statement, private val falseExpr: Statement? = null) : Statement {
    override fun execute() {
        if (cond.eval().asBoolean()) {
            trueExpr.execute()
        } else falseExpr?.execute()
    }

    override fun toString(): String {
        return "IfElseStatement(cond=$cond, trueExpr=$trueExpr, falseExpr=$falseExpr)"
    }
}