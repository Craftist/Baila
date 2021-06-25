package parser.ast.statements

import parser.ast.expressions.Expression
import stdlib.NameTable
import stdlib.libstruct.BailaType
import stdlib.values.primitives.NumberValue

class ForStatement(private val variable: String, private val initial: Expression, private val final: Expression, private val step: Expression, private val body: Statement) : Statement {
    override fun execute() {
        NameTable.pushScope()

        val initial = initial.eval()
        val final = final.eval()

        NameTable.add(variable, BailaType("Number"), initial)
        val v = NameTable.get(variable)


        if (initial.asNumber() < final.asNumber()) {
            val increment = step.eval().asNumber()
            while (v.value.asNumber() <= final.asNumber()) {
                body.execute()
                NameTable.set(variable, NumberValue(v.value.asNumber() + increment))
            }
        } else {
            val increment = -step.eval().asNumber()
            while (v.value.asNumber() >= final.asNumber()) {
                body.execute()
                NameTable.set(variable, NumberValue(v.value.asNumber() + increment))
            }
        }

        NameTable.popScope()
    }

    override fun toString(): String {
        return "ForStatement(variable=$variable, initial=$initial, final=$final, step=$step, body=$body)"
    }
}

// func perfUser { var s = 0 for i = 0 to 99 { s = s + 3 s = s * 2 s = s - 1 } return "perfUser: ${s}" }