package parser.ast.expressions

import parser.tokenizer.TokenType
import stdlib.values.Value
import java.lang.Exception

class BinaryExpression(private val operationToken: TokenType, private val left:Expression, private val right:Expression) : Expression {
    override fun eval(): Value {
        val leftValue = left.eval()
        val rightValue = right.eval()

        return when (operationToken) {
            TokenType.Plus -> Value.add(leftValue, rightValue)
            TokenType.Minus -> Value.sub(leftValue, rightValue)
            TokenType.Star -> Value.mul(leftValue, rightValue)
            TokenType.Slash -> Value.div(leftValue, rightValue)
            TokenType.StarStar -> Value.pow(leftValue, rightValue)
            TokenType.SlashSlash -> Value.intdiv(leftValue, rightValue)
            TokenType.Percent -> Value.rem(leftValue, rightValue)

            TokenType.Amp -> Value.band(leftValue, rightValue)
            TokenType.Bar -> Value.bor(leftValue, rightValue)
            TokenType.Caret -> Value.bxor(leftValue, rightValue)

            TokenType.EqEq -> Value.eq(leftValue, rightValue)
            TokenType.ExclEq -> Value.neq(leftValue, rightValue)

            TokenType.Lt -> Value.lt(leftValue, rightValue)
            TokenType.LtEq -> Value.le(leftValue, rightValue)
            TokenType.Gt -> Value.gt(leftValue, rightValue)
            TokenType.GtEq -> Value.ge(leftValue, rightValue)

            else -> throw Exception("Unknown token in binary operation: $operationToken")
        }
    }

//    override fun getType(): Type {
//        if (operationToken == TokenType.Plus) {
//
//        }
//        if (operationToken == TokenType.Minus) {
//
//        }
//        if (operationToken == TokenType.Star) {
//
//        }
//        if (operationToken == TokenType.Slash) {
//
//        }
//        if (operationToken == TokenType.StarStar) {
//
//        }
//        if (operationToken == TokenType.SlashSlash) {
//
//        }
//        if (operationToken == TokenType.Percent) {
//
//        }
//        if (operationToken == TokenType.Amp) {
//
//        }
//        if (operationToken == TokenType.Bar) {
//
//        }
//        if (operationToken == TokenType.Caret) {
//
//        }
//        if (operationToken == TokenType.EqEq) {
//
//        }
//        if (operationToken == TokenType.ExclEq) {
//
//        }
//        if (operationToken == TokenType.Lt ) {
//
//        }
//        if (operationToken == TokenType.LtEq) {
//
//        }
//        if (operationToken == TokenType.Gt) {
//
//        }
//        if (operationToken == TokenType.GtEq) {
//
//        }
//        return Type("Null")
//    }

    override fun toString(): String {
        return "BinaryExpression($left ${operationToken.type} $right)"
    }

    override fun asCodeRepr(): String = "${left.asCodeRepr()} $operationToken ${right.asCodeRepr()}"
}