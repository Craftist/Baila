package parser.ast.statements

class Statements : Statement {
    val statements = ArrayList<Statement>()

    fun addStatement(stmt: Statement) {
        statements.add(stmt)
    }

    fun getLastStatement() : Statement? {
        return if (statements.size == 0) null else statements[statements.size - 1]
    }

    override fun execute() {
        for (stmt in statements) {
            stmt.execute()
        }
    }

    override fun toString(): String {
        return "Statements (${statements.size} statements) {\n" +
                statements.joinToString("\n") { it.toString() } +
                "\n}"
    }
}