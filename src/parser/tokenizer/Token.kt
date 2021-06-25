package parser.tokenizer

class Token (
        private val cursor: Cursor,
        private val type: TokenType,
        private val value: String = "") {

    fun getLineNo() : Int {
        return cursor.line
    }
    fun getLineCol() : Int {
        return cursor.column
    }
    fun getPosition() : Int {
        return cursor.index
    }
    fun getFileName() : String {
        return cursor.fileName
    }
    fun getCodeLine() : String {
        return cursor.codeLine
    }
    fun getType() : TokenType {
        return type
    }
    fun getValue() : String {
        return value
    }

    override fun toString(): String {
        return "%s%s".format(type, if (value.isEmpty()) "" else "($value)")
    }
}