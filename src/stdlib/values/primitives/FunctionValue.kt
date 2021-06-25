package stdlib.values.primitives

import stdlib.libstruct.BailaType
import stdlib.libstruct.functions.FunctionOverload
import stdlib.libstruct.functions.Parameter
import stdlib.values.Value

class FunctionValue : Value() {
    val overloads = ArrayList<FunctionOverload>()
    var name = ""

    override fun asNumber() = 0.0
    override fun asString() = "[function" + if (name.isEmpty()) "]" else " $name]"
    override fun asBoolean() = false
    override fun asFunction() = overloads

    override fun toString() = asString()
    override fun getType() = BailaType("Function")
    override fun getDefaultValue() = FunctionValue()

    fun addOverload(overload: FunctionOverload) : FunctionValue {
        overloads.add(overload)
        return this
    }

    fun hasOverload(overload: FunctionOverload) : Boolean {
        val found = overloads.filter { ov -> ov.parameters.map { pars -> Parameter("", pars.type, pars.defaultValue) } == overload.parameters.map { pars -> Parameter("", pars.type, pars.defaultValue) } }
        return found.isNotEmpty()
    }

    companion object {
        fun withOverload(overload: FunctionOverload) = FunctionValue().addOverload(overload)
        fun withOverloads(vararg overload: FunctionOverload) : FunctionValue {
            val fval = FunctionValue()
            overload.forEach { fval.addOverload(it) }
            return fval
        }
    }
}