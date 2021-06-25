import parser.ast.Parser
import parser.ast.statements.ExpressionStatement
import parser.tokenizer.Lexer
import parser.tokenizer.Token
import parser.tokenizer.TokenType
import stdlib.NameTable
import stdlib.values.objects.ObjectValue
import stdlib.values.primitives.BooleanValue
import stdlib.values.primitives.FunctionValue
import stdlib.values.primitives.NumberValue
import java.io.File
import java.lang.StringBuilder
import kotlin.io.path.Path

var parensBalance = 0
var prevParensBalance = 0
var indent = 0

val inputSB = StringBuilder()

fun main(args: Array<String>) {
    var showTokens = false
    var showAst = false
    var perf = false

    println("Baila v0.1")
    println(":load to evalute the contents of a file as if it was input into repl")
    println(":vars to show variables in current session")
    // println(":addNum <name> <value> to add a Number variable")
    // println(":addBool <name> <value> to add a Boolean variable")
    println(":perf to toggle performance measuring")
    println(":tokens to toggle tokens show")
    println(":ast to toggle ast show")
    println(":debug to toggle debug mode")
    println(":break to toggle conditional breakpoints")

    // TODO rewrite the entire repl so that it works correctly
    while (true) {
        print(if (parensBalance > 0) "." + "..".repeat(indent) + " " else "> ")
        var inp = readLine() ?: ""
        inputSB.append("$inp ")

        if (inputSB.toString() == "$inp ") {
            val commandArguments = inp.split(' ')
            if (commandArguments[0] == ":vars") {
                Debug.showVars()
                inputSB.delete(0, inputSB.length)
                continue
            } else if (commandArguments[0] == ":load") {
                val filename = commandArguments.slice(1 until commandArguments.size).joinToString(" ")
                val absPath = Path(filename).toAbsolutePath()
                val cts = File(absPath.toString()).readText()
                println("Evaluated file $absPath")
                inputSB.clear()
                inputSB.append(cts.replace('\n', ';'))
            } else if (commandArguments[0] == ":addNum") {
                NameTable.scope.addVariableInferred(commandArguments[1], NumberValue(commandArguments[2].toDouble()))
                inputSB.delete(0, inputSB.length)
                continue
            } else if (commandArguments[0] == ":addBool") {
                NameTable.scope.addVariableInferred(commandArguments[1], BooleanValue(commandArguments[2].toDouble() != 0.0))
                inputSB.delete(0, inputSB.length)
                continue
            } else if (commandArguments[0] == ":tokens") {
                showTokens = !showTokens
                println("Token display toggled.")
                inputSB.delete(0, inputSB.length)
                continue
            } else if (commandArguments[0] == ":ast") {
                showAst = !showAst
                println("Ast display toggled.")
                inputSB.delete(0, inputSB.length)
                continue
            } else if (commandArguments[0] == ":debug") {
                Debug.enabled = !Debug.enabled
                println("Debug information display status: o${if (Debug.enabled) "n" else "ff"}")
                inputSB.delete(0, inputSB.length)
                continue
            } else if (commandArguments[0] == ":break") {
                Debug.conditionalBreakpointsEnabled = !Debug.conditionalBreakpointsEnabled
                println("Conditional breakpoints status: o${if (Debug.conditionalBreakpointsEnabled) "n" else "ff"}")
                inputSB.delete(0, inputSB.length)
                continue
            } else if (commandArguments[0] == ":perf") {
                perf = !perf
                println("Performance display status: o${if (perf) "n" else "ff"}")
                inputSB.delete(0, inputSB.length)
                continue
            } else if (commandArguments[0] == ":typeof") {
                val evalStr = commandArguments.slice(1 until commandArguments.size).joinToString(" ")

                val lexer = Lexer(evalStr, "<REPL>")
                lexer.tokenize()
                val parser = Parser(lexer.tokens)

                try {
                    val stmt = parser.parse()
                    val lastStmt = stmt.getLastStatement() as? ExpressionStatement
                        ?: throw Exception("Cannot get type of not expression")
                    val value = lastStmt.expr.eval()
                    println("Type of the expression: ${value.getType()}")
                    println("Value of the expression: $value")
                } catch (e: Exception) {
                    print("\u001b[31m")
                    if (Debug.enabled)
                        e.printStackTrace()
                    else
                        println(e.javaClass.simpleName + ": " + e.message)
                    print("\u001b[0m")
                    inputSB.delete(0, inputSB.length)
                    continue
                }

                inputSB.delete(0, inputSB.length)
                continue

            } else if (commandArguments[0] == ":file") {
                val filepath = commandArguments[1]
                println("Evaluating the source code in the file '$filepath'")

                val file = File(filepath)
                if (!file.canRead()) {
                    println("Can't read file '$filepath'")
                    inputSB.delete(0, inputSB.length)
                    continue
                }

                val fileCts = file.readText()
                inp = fileCts // no continue because we continue lexing, we just subtituted the repl input code with file cts
                inputSB.delete(0, inputSB.length)
            }
        }


        val lexer = Lexer(inputSB.toString(), "<REPL>") //"var x: String = new String(\"hello, world!\", 10, 5_3)")

        val lexerBeginNanos = System.nanoTime()
        lexer.tokenize()
        val lexerElapsedNanos = System.nanoTime() - lexerBeginNanos

        if (showTokens) {
            println("=== Tokens Debug ===")
            for (tok: Token in lexer.tokens) {
                println(tok.toString())
            }
            println("====================")
            println()
        }

        balanceParens(Lexer(inp, "<REPL>").tokenize().tokens)

        if (parensBalance > 0) {
            if (indent > 0) {
                if (parensBalance > prevParensBalance) {
                    indent++
                } else if (parensBalance < prevParensBalance) {
                    indent--
                }
            } else {
                indent++
            }
            prevParensBalance = parensBalance
            continue // unfinished code
        }

        inputSB.setLength(0)

        val parser = Parser(lexer.tokens)
        try {
            val parserBeginNanos = System.nanoTime()
            val value = parser.parse()
            val parserElapsedNanos = System.nanoTime() - parserBeginNanos

            if (showAst) {
                println("=== AST Debug ===")
                for (stmt in value.statements) {
                    try {
                        println(Debug.indentAstString(stmt))
                    } catch (e: StringIndexOutOfBoundsException) {
                        println("Caught exception while trying to format AST node, falling back to unformatted: ")
                        println(stmt.toString())
                    }
                }
                println("=================")
                println()
            }

            val lastStatement = value.getLastStatement()

            val evaluationBeginNanos = System.nanoTime()
            if (lastStatement is ExpressionStatement) {
                value.statements.slice(0 until value.statements.size - 1).forEach { it.execute() }
                println(lastStatement.expr.eval())
            } else {
                value.execute()
            }
            val evaluationElapsedNanos = System.nanoTime() - evaluationBeginNanos

            if (perf) {
                println("Lexing state elapsed time: %f ms (%f s, $lexerElapsedNanos ns)".format(lexerElapsedNanos / 1e6, lexerElapsedNanos / 1e9))
                println("Parsing state elapsed time: %f ms (%f s, $parserElapsedNanos ns)".format(parserElapsedNanos / 1e6, parserElapsedNanos / 1e9))
                println("Evaluation state elapsed time: %f ms (%f s, $evaluationElapsedNanos ns)".format(evaluationElapsedNanos / 1e6, evaluationElapsedNanos / 1e9))
            }
        } catch (e: Exception) {
            print("\u001b[31m")
            if (Debug.enabled)
                e.printStackTrace(System.out)
            else
                println(e.javaClass.simpleName + ": " + e.message)
            print("\u001b[0m")
            continue
        }
    }
}

fun balanceParens(tokens: List<Token>) {
    for (tok in tokens) {
        if (tok.getType() == TokenType.LeftBracket || tok.getType() == TokenType.LeftParen
            || tok.getType() == TokenType.LeftCurly
        ) {
            parensBalance += 1
        } else if (tok.getType() == TokenType.RightBracket || tok.getType() == TokenType.RightParen
            || tok.getType() == TokenType.RightCurly
        ) {
            parensBalance -= 1
        }
    }
}

class Debug {
    companion object {
        var enabled = false
        var conditionalBreakpointsEnabled = false

        val colors = arrayListOf(
            "\u001b[31m",
            "\u001b[32m",
            "\u001b[33m",
            "\u001b[34m",
            "\u001b[35m",
            "\u001b[36m",
            "\u001b[31;1m",
            "\u001b[32;1m",
            "\u001b[33;1m",
            "\u001b[34;1m",
            "\u001b[35;1m",
            "\u001b[36;1m"
        )
        val colorReset = "\u001b[0m"

        fun log(vararg args: Any) {
            if (!enabled) return

            print("\u001B[33m[DEBUG]\u001B[0m ")
            for (arg in args) {
                print("$arg ".replace("\n", "\n        "))
            }
            println("\u001B[0m")
        }

        fun indentAstString(o: Any, nesting: Int = 0): String {
            val s = o.toString()
            val name = if (s.contains('('))
                s.substring(0 until s.indexOf('('))
            else
                s

            //println("===\ns = $s\nname = $name\n")

            val inner = if (s.contains('(') && s.contains(')'))
                s.substring((s.indexOf('(') + 1) until s.lastIndexOf(')'))
            else
                ""

            //println("\ninner = $inner\n===")

            val coloredOpeningParen = colors[nesting % colors.size] + '('
            val coloredClosingParen = colors[nesting % colors.size] + ')'

            var ret = if (inner.isEmpty()) {
                "  ".repeat(nesting) + name
            } else {
                "  ".repeat(nesting) + name + coloredOpeningParen + "\n" +
                        "  ".repeat(nesting + 1) + indentAstString(inner, nesting + 1) + "\n" +
                        "    ".repeat(nesting) + coloredClosingParen
            }

            //println("\n${colorReset}ret = $ret\n===")

            if (nesting == 0) ret += colorReset

            return ret
        }

        fun showVars() {
            println("Name table:")
            println("  Variables:")
            for (i in NameTable.scope.members) {
                println("    var ${i.key}: ${i.value.type} = ${i.value.value}")
                val v = i.value.value
                if (v is FunctionValue) {
                    for (o in v.overloads) {
                        println(
                            "      • (${o.parameters.joinToString(", ") { "${it.name}: ${it.type}${if (it.defaultValue == null) "" else " = ${it.defaultValue}"}" }}) -> ${
                                o.returnType
                                    ?: "void"
                            }"
                        )
                    }
                } else if (v is ObjectValue) {
                    println("    • Heap Index: ${v.objectHeapIndex ?: "null"}")
                }
            }
        }
    }
}