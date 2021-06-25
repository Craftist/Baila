package parser.ast.expressions

import parser.tokenizer.TokenType
import stdlib.NameTable
import stdlib.values.Value

class AssignmentExpression(private val left: String, private val right: Expression, private val operatorType: TokenType = TokenType.Eq) : Expression {
    override fun eval(): Value {
        if (!NameTable.exists(left)) {
            throw Exception("NameTableMember %s is not defined".format(left))
        }

        val value = when (operatorType) {
            TokenType.Eq -> right.eval()
            TokenType.PlusEq -> Value.add(NameTable.scope.getVariable(left).value, right.eval())
            TokenType.MinusEq -> Value.sub(NameTable.scope.getVariable(left).value, right.eval())
            TokenType.StarEq -> Value.mul(NameTable.scope.getVariable(left).value, right.eval())
            TokenType.StarStarEq -> Value.pow(NameTable.scope.getVariable(left).value, right.eval())
            TokenType.SlashEq -> Value.div(NameTable.scope.getVariable(left).value, right.eval())
            TokenType.SlashSlashEq -> Value.intdiv(NameTable.scope.getVariable(left).value, right.eval())
            TokenType.PercentEq -> Value.rem(NameTable.scope.getVariable(left).value, right.eval())
            TokenType.AmpEq -> Value.band(NameTable.scope.getVariable(left).value, right.eval())
            TokenType.BarEq -> Value.bor(NameTable.scope.getVariable(left).value, right.eval())
            TokenType.CaretEq -> Value.bxor(NameTable.scope.getVariable(left).value, right.eval())
            else -> throw Exception("Unknown operatorType in AssignmentExpression: $operatorType")
        }

        NameTable.set(left, value)
        return value
    }

//    fun getType(): Type {
//        return right.getType()
//    }

    override fun toString(): String {
        return "AssignmentExpression($left = $right)"
    }

    override fun asCodeRepr(): String = "$left $operatorType ${right.asCodeRepr()}"
}