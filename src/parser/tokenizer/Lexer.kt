package parser.tokenizer

import parser.ast.errors.SyntaxError

class Lexer(private val input: String, private val fileName: String) {
    private var lineno: Int = 1
    private var linecol: Int = 1
    private var pos: Int = 0
    private val length: Int = input.length
    private val buffer: StringBuilder = StringBuilder(length)

    val tokens: ArrayList<Token> = ArrayList()

    companion object {
        private val operators: HashMap<String, TokenType> = HashMap()
        private val keywords: HashMap<String, TokenType> = HashMap()

        fun isIdentifierStart(ch: Char) : Boolean {
            return ch.toLowerCase() in 'a'..'z' || ch == '_' || ch == '$'
        }

        fun isIdentifierPart(ch: Char) : Boolean {
            return ch.toLowerCase() in 'a'..'z' || ch in '0'..'9' || ch == '_' || ch == '$'
        }

        init {
            operators["+"] = TokenType.Plus
            operators["+="] = TokenType.PlusEq
            operators["++"] = TokenType.PlusPlus
            operators["-"] = TokenType.Minus
            operators["-="] = TokenType.MinusEq
            operators["--"] = TokenType.MinusMinus
            operators["*"] = TokenType.Star
            operators["**"] = TokenType.StarStar
            operators["*="] = TokenType.StarEq
            operators["**="] = TokenType.StarStarEq
            operators["/"] = TokenType.Slash
            operators["//"] = TokenType.SlashSlash
            operators["/="] = TokenType.SlashEq
            operators["//="] = TokenType.SlashSlashEq
            operators["%"] = TokenType.Percent
            operators["%="] = TokenType.PercentEq
            operators["="] = TokenType.Eq
            operators["=="] = TokenType.EqEq
            operators["==="] = TokenType.EqEqEq
            operators["!="] = TokenType.ExclEq
            operators["!=="] = TokenType.ExclEqEq
            operators["|"] = TokenType.Bar
            operators["||"] = TokenType.BarBar
            operators["|="] = TokenType.BarEq
            operators["||="] = TokenType.BarBarEq
            operators["|>"] = TokenType.Pipe
            operators["&"] = TokenType.Amp
            operators["&&"] = TokenType.AmpAmp
            operators["&="] = TokenType.AmpEq
            operators["&&="] = TokenType.AmpAmpEq
            operators["~"] = TokenType.Tilde
            operators["^"] = TokenType.Caret
            operators["^="] = TokenType.CaretEq
            operators["^^"] = TokenType.CaretCaret
            operators["^^="] = TokenType.CaretCaretEq
            operators["."] = TokenType.Dot
            operators[".."] = TokenType.DotDot
            operators[","] = TokenType.Comma
            operators["?."] = TokenType.NullDot
            operators["??"] = TokenType.Elvis
            operators["??="] = TokenType.ElvisEq
            operators["<"] = TokenType.Lt
            operators["<="] = TokenType.LtEq
            operators["<<"] = TokenType.LtLt
            operators["<<="] = TokenType.LtLtEq
            operators[">"] = TokenType.Gt
            operators[">="] = TokenType.GtEq
            operators[">>"] = TokenType.GtGt
            operators[">>="] = TokenType.GtGtEq
            operators[">>>"] = TokenType.GtGtGt
            operators[">>>="] = TokenType.GtGtGtEq
            operators["->"] = TokenType.SingleArrow
            operators["=>"] = TokenType.DoubleArrow

            operators["("] = TokenType.LeftParen
            operators[")"] = TokenType.RightParen
            operators["["] = TokenType.LeftBracket
            operators["]"] = TokenType.RightBracket
            operators["{"] = TokenType.LeftCurly
            operators["}"] = TokenType.RightCurly

            operators["!"] = TokenType.Excl
            operators["?"] = TokenType.Question
            operators[":"] = TokenType.Colon
            operators["::"] = TokenType.ColonColon
            operators[";"] = TokenType.Semicolon
        }

        init {
            keywords["null"] = TokenType.Null
            keywords["true"] = TokenType.True
            keywords["false"] = TokenType.False
            keywords["this"] = TokenType.This
            keywords["super"] = TokenType.Super

            keywords["var"] = TokenType.Var
            keywords["const"] = TokenType.Const
            keywords["prop"] = TokenType.Property
            keywords["property"] = TokenType.Property
            keywords["func"] = TokenType.Function
            keywords["function"] = TokenType.Function
            keywords["class"] = TokenType.Class
            keywords["struct"] = TokenType.Struct
            keywords["interface"] = TokenType.Interface
            keywords["enum"] = TokenType.Enum
            keywords["operator"] = TokenType.Operator

            keywords["constructor"] = TokenType.Constructor
            keywords["deconstructor"] = TokenType.Deconstructor

            keywords["typeof"] = TokenType.Typeof
            keywords["from"] = TokenType.From
            keywords["import"] = TokenType.Import
            keywords["export"] = TokenType.Export

            keywords["ref"] = TokenType.Ref

            keywords["if"] = TokenType.If
            keywords["else"] = TokenType.Else
            keywords["switch"] = TokenType.Switch
            keywords["for"] = TokenType.For
            keywords["do"] = TokenType.Do
            keywords["while"] = TokenType.While
            keywords["try"] = TokenType.Try
            keywords["catch"] = TokenType.Catch
            keywords["finally"] = TokenType.Finally

            keywords["global"] = TokenType.Global
            keywords["public"] = TokenType.Public
            keywords["private"] = TokenType.Private
            keywords["protected"] = TokenType.Protected
            keywords["override"] = TokenType.Override
            keywords["sealed"] = TokenType.Sealed
            keywords["static"] = TokenType.Static

            keywords["async"] = TokenType.Async
            keywords["await"] = TokenType.Await

            // Keywords that are actually operators by logic
            keywords["break"] = TokenType.Break
            keywords["continue"] = TokenType.Continue
            keywords["throw"] = TokenType.Throw
            keywords["return"] = TokenType.Return
            keywords["yield"] = TokenType.Yield
            keywords["in"] = TokenType.In
            keywords["is"] = TokenType.Is
            keywords["as"] = TokenType.As
        }
    }

    private enum class ParseState {
        /**
         * Parse value (number, string, regex, etc)
         */
        Value,
        /**
         * Parse operator (excluding parentheses)
         */
        Operator
    }

    // *******************************************

    fun tokenize() : Lexer {
        var state = ParseState.Value

        // Checking for ParseState.Value implies that
        //   you are tokenizing either value or something that should precede a value
        // Checking for ParseState.Operator implies that
        //   you are tokenizing something that should be between values.
        // Most of the time state should switch.

        while (pos < length) {
            val ch = peek(); val nextch = peek(1)

            if (ch != '0' && Character.isDigit(ch) || ch == '.' && Character.isDigit(nextch)) {
                tokenizeDecimalNumber()
                state = ParseState.Operator
            } else if (ch == '0') {
                when (nextch.toLowerCase()) {
                    'b' -> {
                        next(); next()
                        tokenizeBinaryNumber()
                    }
                    'o' -> {
                        next(); next()
                        tokenizeOctalNumber()
                    }
                    'x' -> {
                        next(); next()
                        tokenizeHexadecimalNumber()
                    }
                    else -> {
                        tokenizeDecimalNumber()
                    }
                }
                state = ParseState.Operator
            } else if (ch == '\'') {
                next()
                tokenizeSingleString()
                state = ParseState.Operator
            } else if (ch == '"') {
                next()
                tokenizeDoubleString()
                state = ParseState.Operator
            } else if (ch == '`') {
                next()
                tokenizeVerbatimString()
                state = ParseState.Operator
            } else if (isIdentifierStart(ch)) {
                tokenizeIdentifier()
                state = ParseState.Operator
            } else if (state == ParseState.Value && ch == '/') {
                next() // skip first slash
                tokenizeRegex()
                state = ParseState.Operator
            } else if (state == ParseState.Operator && ch == '!' && nextch == 'i' && peek(2) == 'n') {
                next()
                next()
                next()
                addToken(TokenType.NotIn)
                state = ParseState.Value
            } else if (state == ParseState.Operator && ch == '!' && nextch == 'i' && peek(2) == 's') {
                next()
                next()
                next()
                addToken(TokenType.IsNot)
                state = ParseState.Value
            } else if (state == ParseState.Operator && ch == '?' && nextch == 'a' && peek(2) == 's') {
                next()
                next()
                next()
                addToken(TokenType.NullableAs)
                state = ParseState.Value
            } else if (state == ParseState.Operator && "+-*/%=!~&|^~?:;<>.,".indexOf(ch) != -1) {
                if (ch == '/' && nextch == '*') {
                    next(); next()
                    tokenizeMultilineComment()
                } else {
                    tokenizeOperator()
                    state = ParseState.Value
                }
            } else if (state == ParseState.Value && "+-*!~?<".indexOf(ch) != -1) {
                tokenizeOperator()
            } else if ("()[]{}".indexOf(ch) != -1) {
                tokenizeOperator()
                state = ParseState.Operator
            } else if (ch == '#') {
                next()
                tokenizeComment()
            } else {
                // whitespace
                next()
            }
        }
        return this
    }

    // *******************************************

    private fun tokenizeDecimalNumber() {
        clearBuffer()
        var ch = peek()
        while (pos < length) {
            if (ch == 'f') { // float suffix
                buffer.append('f')
                next() // skip f
                break
            }
            if (ch == 'c') { // char suffix
                buffer.append('c')
                next() // skip c
                break
            }
            if (ch == '_') {
                ch = next()
                continue
            }
            if (ch == '.' && buffer.isEmpty()) buffer.append('0')
            if (ch == '.' && buffer.indexOf('.') != -1 && peek(1).isDigit()) throw SyntaxError("Encountered second decimal point in a number literal")
            if (ch == '.' && !peek(1).isDigit()) break
            if (!Character.isDigit(ch) && ch != '_' && ch != '.') break
            buffer.append(ch)
            ch = next()
        }
        addToken(TokenType.NumberLiteral, buffer.toString())
    }

    private fun tokenizeBinaryNumber() {
        clearBuffer()
        var ch = peek()
        while (ch == '0' || ch == '1' || ch == '_') {
            if (ch == '_') {
                ch = next()
                continue
            }
            buffer.append(ch)
            ch = next()
        }
        addToken(TokenType.NumberLiteral, buffer.toString().toInt(2).toString())
    }

    private fun tokenizeOctalNumber() {
        clearBuffer()
        var ch = peek()
        while (ch in '0'..'7' || ch == '_') {
            if (ch == '_') {
                ch = next()
                continue
            }
            buffer.append(ch)
            ch = next()
        }
        addToken(TokenType.NumberLiteral, buffer.toString().toInt(8).toString())
    }

    private fun tokenizeHexadecimalNumber() {
        clearBuffer()
        var ch = peek().toLowerCase()
        while (ch in '0'..'9' || ch in 'a'..'f' || ch == '_') {
            if (ch == '_') {
                ch = next()
                continue
            }
            buffer.append(ch)
            ch = next().toLowerCase()
        }
        addToken(TokenType.NumberLiteral, buffer.toString().toInt(16).toString())
    }

    private fun tokenizeSingleString() {
        clearBuffer()
        var ch = peek()
        var unclosedCurlies = 0
        while (pos < length) {
            if (ch == '{') {
                ch = next()
                Debug.log("LEXING, { encountered, add unclosedCurlies")
                ++unclosedCurlies
                buffer.append("{")
                continue
            }
            if (ch == '}') {
                ch = next()
                Debug.log("LEXING, } encountered, remove unclosedCurlies")
                --unclosedCurlies
                buffer.append("}")
                continue
            }
            // TODO fix interpolating (make it like in Kotlin)
            if (ch == '\\') {
                // escape sequences
                ch = next()
                when (ch) {
                    '$' -> {
                        buffer.append("\\$")
                        ch = next()
                    }
                    '\'' -> {
                        buffer.append('\'')
                        ch = next()
                    }
                    '\\' -> {
                        buffer.append('\\')
                        ch = next()
                    }
                    'n' -> {
                        buffer.append('\n')
                        ch = next()
                    }
                    'r' -> {
                        buffer.append('\r')
                        ch = next()
                    }
                    't' -> {
                        buffer.append('\t')
                        ch = next()
                    }
                    'b' -> {
                        buffer.append('\b')
                        ch = next()
                    }
                    '0' -> {
                        buffer.append('\u0000')
                        ch = next()
                    }
                }
            }
            if (ch == '\'' && unclosedCurlies == 0) break
            if (ch == '\u0000') throw Exception("Unclosed string")
            buffer.append(ch)
            ch = next()
        }
        if (unclosedCurlies != 0) throw Exception("Unbalanced curly brackets inside template string")
        next() // skip closing quote
        addToken(TokenType.StringLiteral, buffer.toString())
    }

    private fun tokenizeDoubleString() {
        clearBuffer()
        var ch = peek()
        var unclosedCurlies = 0
        while (pos < length) {
            if (ch == '{') {
                ch = next()
                if (ch == '{') {
                    buffer.append("{{")
                    ch = next()
                } else {
                    ++unclosedCurlies
                    buffer.append("{")
                }
                continue
            }
            if (ch == '}') {
                ch = next()
                if (ch == '}') {
                    buffer.append("}}")
                    ch = next()
                } else {
                    --unclosedCurlies
                    buffer.append("}")
                }
                continue
            }
            if (ch == '\\') {
                // escape sequences
                ch = next()
                when (ch) {
                    '\"' -> {
                        buffer.append('\'')
                        ch = next()
                    }
                    '\\' -> {
                        buffer.append('\\')
                        ch = next()
                    }
                    'n' -> {
                        buffer.append('\n')
                        ch = next()
                    }
                    'r' -> {
                        buffer.append('\r')
                        ch = next()
                    }
                    't' -> {
                        buffer.append('\t')
                        ch = next()
                    }
                    'b' -> {
                        buffer.append('\b')
                        ch = next()
                    }
                    '0' -> {
                        buffer.append('\u0000')
                        ch = next()
                    }
                }
            }
            if (ch == '\"' && unclosedCurlies == 0) break
            if (ch == '\u0000') throw Exception("Unclosed string")
            buffer.append(ch)
            ch = next()
        }
        if (unclosedCurlies != 0) throw Exception("Unbalanced curly brackets inside template string")
        next() // skip closing quote
        addToken(TokenType.StringLiteral, buffer.toString())
    }

    private fun tokenizeVerbatimString() {
        clearBuffer()
        var ch = peek()
        var unclosedCurlies = 0
        while (pos < length) {
            if (ch == '{') {
                ch = next()
                if (ch == '{') {
                    buffer.append("{{")
                    ch = next()
                } else {
                    ++unclosedCurlies
                    buffer.append("{")
                }
                continue
            }
            if (ch == '}') {
                ch = next()
                if (ch == '}') {
                    buffer.append("}}")
                    ch = next()
                } else {
                    --unclosedCurlies
                    buffer.append("}")
                }
                continue
            }
            if (ch == '`' && unclosedCurlies == 0) break
            if (ch == '\u0000') throw Exception("Unclosed string")
            buffer.append(ch)
            ch = next()
        }
        if (unclosedCurlies != 0) throw Exception("Unbalanced curly brackets inside template string")
        next() // skip closing quote
        addToken(TokenType.StringLiteral, buffer.toString())
    }

    private fun tokenizeIdentifier() {
        clearBuffer()
        var ch = peek()
        while (pos < length) {
            if (!Lexer.isIdentifierPart(ch)) break
            buffer.append(ch)
            ch = next()
        }
        val keyword = keywords[buffer.toString()]
        if (keyword != null) {
            addToken(keyword)
        } else {
            addToken(TokenType.Identifier, buffer.toString())
        }
    }

    private fun tokenizeOperator() {
        clearBuffer()
        var ch = peek()
        while (pos < length) {
            if (!operators.containsKey(buffer.toString() + ch)) break
            buffer.append(ch)
            ch = next()
        }
        val operator = operators[buffer.toString()] ?: throw Exception("operators[buffer.toString()] is null. Buffer='$buffer'")
        addToken(operator)
    }

    private fun tokenizeComment() {
        var ch = peek()
        while (pos < length) {
            if (ch == '\n') break
            ch = next()
        }
    }

    private fun tokenizeMultilineComment() {
        var ch = peek()
        while (pos < length) {
            if (ch == '*' && peek(1) == '/') break
            ch = next()
        }
    }

    private fun tokenizeRegex() {
        clearBuffer()
        var ch = peek()
        var flags = ""
        // parse pattern part (anything until unescaped / is found)
        while (pos < length) {
            if (ch == '/') break
            buffer.append(ch)
            ch = next()
        }
        ch = next() // skip final /
        // parse flags part (any string of subsequent letters)
        while (pos < length) {
            if (!Character.isLetter(ch)) break
            flags += ch
            ch = next()
        }
        addToken(TokenType.RegexLiteral, "/$buffer/$flags")
    }

    // *******************************************

    private fun addToken(type: TokenType) {
        tokens.add(Token(Cursor(lineno, linecol, pos - type.type.length - type.subtractFromPos, fileName, input.split(Regex("\\n"))[lineno-1]), type))
    }

    private fun addToken(type: TokenType, value: String) {
        tokens.add(Token(Cursor(lineno, linecol, pos - value.length - type.subtractFromPos, fileName, input.split(Regex("\\n"))[lineno-1]), type, value))
    }

    private fun clearBuffer() {
        buffer.setLength(0)
    }

    private fun peek(rel: Int = 0) : Char {
        val pos = pos + rel
        if (pos >= length) {
            return '\u0000'
        }
        return input[pos]
    }

    private fun next() : Char {
        ++pos
        val ch = peek()
        if (ch == '\n') {
            ++lineno
            linecol = 1
        }
        return ch
    }
}