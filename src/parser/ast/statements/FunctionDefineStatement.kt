package parser.ast.statements

import stdlib.NameTable
import stdlib.libstruct.BailaType
import stdlib.libstruct.functions.FunctionOverload
import stdlib.libstruct.functions.Parameter
import stdlib.libstruct.functions.StatementCallable
import stdlib.values.primitives.FunctionValue
import java.util.ArrayList

class FunctionDefineStatement(val name: String, val parameters: ArrayList<Parameter>, val body: Statement, val returnType: BailaType) : Statement {
    override fun execute() {
        // overloading happens here
        val overload = FunctionOverload(
                StatementCallable(body),
                parameters,
                returnType
        )

        if (!NameTable.exists(name)) {
            // function doesn't exist, create a variable with a function value with single overload inside
            val func = FunctionValue()
            func.name = name
            func.overloads.add(overload)
            NameTable.addInferred(name, func)
        } else {
            // function already exists, add an overload
            val function = NameTable.get(name).value

            // can overload only functions.
            if (function !is FunctionValue)
                throw Exception("Cannot overload variable '%s' of type '%s'".format(name, function.getType()))

            // check whether such overload already exists
            if (function.hasOverload(overload)) {
                val pars = overload.parameters
                throw Exception("Overload with %s parameters%s already exists".format(
                        if (pars.isEmpty()) "no" else pars.size,
                        if (pars.isEmpty()) "" else " of types ${pars.joinToString(", ") { "'${it.type}'" }}"
                ))
            }

            // check ambiguous overloads
            val thisReqParTypes = parameters.filter { it.defaultValue == null }.map { it.type }
            val clearedOverloadParameters = function.overloads.map { ov ->
                ov.parameters.filter { parameter -> parameter.defaultValue == null }
            }
            if (clearedOverloadParameters.any { ovPars: List<Parameter> -> ovPars.map { it.type } == thisReqParTypes }) {
                throw Exception("Potentially ambiguous overload: (${thisReqParTypes.joinToString (", ")})")
            }

            function.addOverload(overload)
        }
    }

    override fun toString(): String {
        return "FunctionDefineStatement(name=$name, parameters={{ ${parameters.joinToString(", ") { it.toString() }} }}, body=$body, returnType=$returnType)"
    }
}