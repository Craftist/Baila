package parser.ast.expressions

import parser.ast.errors.TypeError
import stdlib.values.Value
import stdlib.values.primitives.ListValue

class IndexerAccessExpression(private val valueExpr: Expression, private val indexExpr: Expression) : Expression {
    override fun eval(): Value {
        val index = indexExpr.eval().asNumber().toInt()
        val value = valueExpr.eval()

        if (value is ListValue) {
            return value.list[index]
        }

        throw TypeError("Cannot access indexer of type '${value.getType()}'")
    }

    override fun toString(): String {
        return "IndexerAccessExpression($valueExpr, $indexExpr)"
    }

    override fun asCodeRepr(): String = "${valueExpr.asCodeRepr()}[${indexExpr.asCodeRepr()}]"
}