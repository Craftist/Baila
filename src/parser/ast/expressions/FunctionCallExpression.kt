package parser.ast.expressions

import parser.ast.errors.TypeError
import stdlib.NameTable
import stdlib.libstruct.functions.FunctionWithOverloads
import stdlib.values.Value
import stdlib.values.objects.ObjectValue


class FunctionCallExpression(private val holder: Expression, private val args: ArrayList<Expression>) : Expression {
    override fun eval(): Value {
        /*
           TODO add list type, that will hold an arbitrary amount of values of
            any type (unless a single generic is specified, then all the values should be of that type)

           TODO varargs, e.g. func f(...args:Number) where args becomes <Number>List

           TODO List spreading using ... operator in function call (f(...args))
            and list literal ([...list1, ...list2] concatenates two lists)
         */

        val functionValue: Value

        if (holder !is VariableExpression) {
            functionValue = holder.eval()
        } else {
            val name = holder.name
            val memory = NameTable.get(name)
            functionValue = memory.value
        }

        // DONE variables like String, Object, TestClass should be of type Type, not of respective types (TestClass should not be of type TestClass)
        // DONE when creating a new object, instance members should be deep copied

        val availableOverloads = if (functionValue is ObjectValue) {
            // Function is a Type, therefore when we call it we expect to call the constructor of that type
            val heapIndex = functionValue.objectHeapIndex ?: throw TypeError.nullReference(functionValue.toString())
            val obj = NameTable.getHeapObject(heapIndex)
            if (obj is stdlib.values.classes.Type) { obj.correspondingTypeContainer.getConstructor() }
            else throw TypeError.objectIsNotCallable(functionValue.toString())
        } else {
            // If it's not a Type, we expect to call a function.
            // If there are more types of callables (not only type and function) to be expected, add them in else if before this else.
            functionValue.asFunction()
        }

        return FunctionWithOverloads(availableOverloads).call(holder, args)!!
    }

    override fun toString(): String {
        return "FunctionCallExpression(holder=$holder, args={{ ${args.joinToString { it.toString() }} }})"
    }

    override fun asCodeRepr(): String = "${holder.asCodeRepr()}(${args.joinToString { it.asCodeRepr() }})"
}