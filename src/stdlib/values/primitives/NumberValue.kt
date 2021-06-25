package stdlib.values.primitives

import stdlib.libstruct.BailaType
import stdlib.libstruct.functions.FunctionOverload
import stdlib.values.Value

class NumberValue(private val value: Double) : Value() {
    override fun asNumber() = value
    override fun asString() = if (value - Math.floor(value) == 0.0) value.toInt().toString() else value.toString()
    override fun asBoolean() = value != 0.0
    override fun asFunction() = arrayListOf<FunctionOverload>()

    override fun toString() = asString()
    override fun getType() = BailaType("Number")
    override fun getDefaultValue() = NumberValue(0.0)
}