package stdlib.values.primitives

import parser.ast.Parser
import parser.ast.statements.ExpressionStatement
import parser.tokenizer.Lexer
import stdlib.NameTable
import stdlib.libstruct.BailaType
import stdlib.libstruct.functions.FunctionOverload
import stdlib.values.Value
import java.lang.StringBuilder
import kotlin.Exception

/* TODO when in interpolation, variable s is not defined in the following code:
     func perfUser { var s = 0 for i = 0 to 99 { s = s + 3 s = s * 2 s = s - 1 } return "perfUser: ${s}" }
     However it works in the concatenation:
       func perfUser { var s = 0 for i = 0 to 99 { s = s + 3 s = s * 2 s = s - 1 } return "perfUser: " + s }
*/

class StringValue(private val value: String) : Value() {
    override fun asNumber() = value.toDouble()
    override fun asString(): String {
        val newString = StringBuilder()
        var current: Char

        var pos = 0
        val len = value.length

        fun peek(rel: Int = 0): Char = if (pos < len) value[pos + rel] else '\u0000'
        fun next(): Char = if (pos < len - 1) value[++pos] else '\u0000'

        current = peek()
        while (pos < len && current != '\u0000') {
            var unclosedCurlies = 0
            if (current == '\\' && peek(1) == '$') {
                next(); next()
                newString.append('$')
                continue
            } else if (current == '$' && peek(1) != '{') {
                // Debug $simpleIdentName
                current = next() // skip $


                Debug.log("into simple identifier name interpolating")
                val ident = StringBuilder()

                if (!Lexer.isIdentifierStart(current))
                    throw Exception("Unallowed identifier start inside interpolation string: '$current'")

                ident.append(current)
                current = next()

                while (pos < len) {
                    Debug.log("CURRENT IN LEXER: $current")
                    if (!Lexer.isIdentifierPart(current)) {
                        break
                    }

                    ident.append(current)
                    current = next()
                }

                val v = NameTable.get(ident.toString())
                newString.append(v.value.asString())
            } else if (current == '$' && peek(1) == '{') {
                // Debug ${expression}

                current = next()
                Debug.log("into { interpolating")

                ++unclosedCurlies
                Debug.log("current=$current, alreadyRead=${value.substring(0, pos)}, ++unclosedCurlies, unclosedCurlies=$unclosedCurlies")

                val interpolateBuffer = StringBuilder()
                current = next() // skip opening {
                while (pos < len) {
                    if (current == '{') {
                        ++unclosedCurlies
                        Debug.log("INSIDE INTERPOLATING current=$current, alreadyRead=${value.substring(0, pos)}, ++unclosedCurlies, unclosedCurlies=$unclosedCurlies")
                    }
                    if (current == '}' && unclosedCurlies != 0) {
                        --unclosedCurlies
                        Debug.log("current=$current, alreadyRead=${value.substring(0, pos)}, --unclosedCurlies, unclosedCurlies=$unclosedCurlies")
                    }
                    if (current == '}' && unclosedCurlies == 0) break
                    if (current == '\u0000') throw Exception("Unclosed template")
                    interpolateBuffer.append(current)

                    current = next()
                }

                Debug.log("Lexing $interpolateBuffer")
                val lexer = Lexer(interpolateBuffer.toString(), "<TEMPLATE_STRING>") // TODO change into actual fileName by passing cursor
                lexer.tokenize()
                val tokens = lexer.tokens
                val parser = Parser(tokens)
                val statement = parser.parse().getLastStatement() as? ExpressionStatement
                        ?: throw Exception("Only expressions are allowed inside interpolated strings")
                newString.append(statement.expr.eval())
            } else if (current == '}') {
                Debug.log("into current == }")
                Debug.log("into } error")
                // found } without {, error
                throw Exception("Found interpolation ending without beginning")
            } else {
                // another symbol
                newString.append(current)
            }

            current = next()
        }

        // $'abc{{123+456}}def'

        return newString.toString()
    }

    override fun asBoolean() = value != ""
    override fun asFunction() = arrayListOf<FunctionOverload>()

    override fun toString() = asString()
    override fun getType() = BailaType("String")

    override fun getDefaultValue() = StringValue("")
}