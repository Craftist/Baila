package parser.tokenizer

data class Cursor(
        val line: Int,
        val column: Int,
        val index: Int,

        val fileName: String,
        val codeLine: String
)