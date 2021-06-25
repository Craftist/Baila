package parser.ast.statements

import stdlib.NameTable

class BlockStatement : Statement {
    val statements = ArrayList<Statement>()

    fun addStatement(stmt: Statement) {
        statements.add(stmt)
    }

    fun getLastStatement() : Statement? {
        return if (statements.size == 0) null else statements[statements.size - 1]
    }

    override fun execute() {
        /*
        FIXME nametableinstance members pointing to the same object even though treemaps are different. (link: https://vk.com/im?msgid=14176755&sel=c983)
          Might as well be changing the stack-based scope to linkedlist-based (with parent scopes and scope finding).
        */
        NameTable.pushScope()
        for (stmt in statements) {
            stmt.execute()
        }
        NameTable.popScope()
    }

    override fun toString(): String {
        return "BlockStatement (${statements.size} statements) {\n" +
                statements.joinToString("\n") { it.toString() } +
                "\n}"
    }
}