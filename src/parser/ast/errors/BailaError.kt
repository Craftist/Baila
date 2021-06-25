package parser.ast.errors

import parser.tokenizer.Token
import parser.tokenizer.TokenType

open class BailaError constructor(override val message: String?) : Exception(message)
{
    init {
        println("MESSAGE BAILAERROR: $message")
    }

    companion object {
        fun showErrorOnLine(line: String, startPos: Int, underlineError: Int = 1) : String {
            Debug.log("showErrorOnLine: line=$line, startPos=$startPos, underlineError=$underlineError")
            return line + "\n" + " ".repeat(if (startPos < 0) 0 else startPos) + "^".repeat(if (underlineError < 0) 0 else underlineError)
        }
    }
}