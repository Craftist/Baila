package stdlib.values.primitives

import stdlib.libstruct.BailaType
import stdlib.libstruct.functions.FunctionOverload
import stdlib.values.Value

class BooleanValue(private val value: Boolean) : Value() {
    override fun asNumber() = if (value) 1.0 else 0.0
    override fun asString() = if (value) "true" else "false"
    override fun asBoolean() = value
    override fun asFunction() = arrayListOf<FunctionOverload>()

    override fun toString() = asString()
    override fun getType() = BailaType("Boolean")
    override fun getDefaultValue() = BooleanValue(false)
}