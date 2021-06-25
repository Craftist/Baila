package parser.ast.statements

import parser.ast.expressions.Expression
import stdlib.NameTable
import stdlib.libstruct.BailaType

class VariableDefineStatement(private val name: String, val type: BailaType?, val value: Expression?) : Statement {
    override fun execute() {
        if (type == null) {
            // Infer from the value
            if (value == null) {
                throw Exception("Error: either type or value should be provided for the variable %s".format(name))
            }

            val evaled = value.eval()
            NameTable.add(name, evaled.getType(), evaled)
            return
        }

        if (value == null) {
            // Infer default value from the type
            NameTable.add(name, type, type.getDefaultValue())
            return
        }

        NameTable.add(name, type, value.eval())
    }

    override fun toString(): String {
        return "VariableDefineStatement(name=$name, type=$type, value=$value)"
    }
}