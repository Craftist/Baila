package parser.ast.expressions

import stdlib.values.primitives.BooleanValue
import stdlib.values.primitives.NumberValue
import stdlib.values.Value

class UnaryExpression(private val operation: Operator, private val op: Expression) : Expression {
    enum class Operator(private val operator: String) {
        LogicalNot("!"),
        BitwiseNot("~"),
        Plus("+"),
        Minus("-");

        override fun toString(): String {
            return operator
        }
    }

    private fun lnot(v: Value) : Value {
        return BooleanValue(!v.asBoolean())
    }

    private fun bnot(v: Value) : Value {
        return NumberValue(v.asNumber().toInt().inv().toDouble())
    }

    private fun plus(v: Value) : Value {
        return NumberValue(v.asNumber())
    }

    private fun minus(v: Value) : Value {
        return NumberValue(-v.asNumber())
    }

    override fun eval(): Value {
        val value = op.eval()

        return when (operation) {
            Operator.LogicalNot -> lnot(value)
            Operator.BitwiseNot -> bnot(value)
            Operator.Plus -> plus(value)
            Operator.Minus -> minus(value)
        }
    }

    override fun toString(): String {
        return "UnaryExpression($operation $op)"
    }

    override fun asCodeRepr(): String = "$operation${op.asCodeRepr()}"
}