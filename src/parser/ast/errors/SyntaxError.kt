package parser.ast.errors

import parser.tokenizer.Token
import parser.tokenizer.TokenType

class SyntaxError constructor(override val message: String?) : Exception(message)
{
    // TODO normal ^^^ highlight
    companion object {
        fun unexpectedToken(unexpected: Token) : SyntaxError {
            val errorOnLine = "\n" + BailaError.showErrorOnLine(unexpected.getCodeLine(), unexpected.getPosition(), unexpected.getType().type.length)

            val unexpectedStr = if (unexpected.getType() != TokenType.EOF) "'${unexpected.getType().type}'" else "<EOF>"
            return if (unexpected.getType() == TokenType.Identifier) {
                // TODO add stack trace and error line showing and pointing (as in Python when syntax error has occurred).
                SyntaxError("Unexpected identifier $unexpectedStr$errorOnLine")
            } else {
                SyntaxError("Unexpected token $unexpectedStr$errorOnLine")
            }
        }

        fun unexpectedToken(unexpected: Token, expectedType: TokenType) : SyntaxError {
            return unexpectedToken(unexpected, arrayOf(expectedType))
        }

        fun unexpectedToken(unexpected: Token, expectedTypes: Array<TokenType>) : SyntaxError {
            val errorOnLine = "\n" + BailaError.showErrorOnLine(unexpected.getCodeLine(), unexpected.getPosition() - 1, unexpected.getType().type.length)

            val unexpectedType = unexpected.getType()
            val unexpectedStr = if (unexpectedType == TokenType.EOF) "'${unexpectedType.type}'" else "<EOF>"

            return SyntaxError("Unexpected $unexpectedStr, expected token${if (expectedTypes.size > 1) "s" else ""}: ${expectedTypes.joinToString { "'${it.type}'" }}" + errorOnLine)
            /*return if (unexpectedType == TokenType.Identifier && expectedType == TokenType.Identifier) {
                SyntaxError("Unexpected identifier $unexpectedStr, '${expectedType.type}' expected" + errorOnLine)
            } else if (unexpectedType == TokenType.Identifier && expectedType != TokenType.Identifier) {
                SyntaxError("Unexpected identifier $unexpectedStr, token '${expectedType.type}' expected" + errorOnLine)
            } else if (unexpectedType != TokenType.Identifier && expectedType == TokenType.Identifier) {
                SyntaxError("Unexpected token $unexpectedStr, identifier '${expectedType.type}' expected" + errorOnLine)
            } else {
                SyntaxError("Unexpected token $unexpectedStr,  '${expectedType.type}' expected" + "\n" + errorOnLine)
            }*/
        }

        fun duplicateStaticModifier(className: String): SyntaxError {
            return SyntaxError("Duplicate static modifier in $className") // TODO lookahead to also output member name
        }

        fun duplicateAccessibilityModifier(className: String): SyntaxError {
            return SyntaxError("Duplicate accessibility modifier in $className") // TODO lookahead to also output member name
        }

        fun duplicateGetter(className: String, propName: String) : SyntaxError {
            return SyntaxError("Duplicate getter in $className.$propName")
        }

        fun duplicateSetter(className: String, propName: String) : SyntaxError {
            return SyntaxError("Duplicate setter in $className.$propName")
        }
    }
}