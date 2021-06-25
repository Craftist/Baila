package stdlib.values.primitives

import stdlib.libstruct.BailaType
import stdlib.libstruct.functions.FunctionOverload
import stdlib.values.Value

class ListValue() : Value() {
    val list = ArrayList<Value>()

    override fun asNumber() = 0.0
    override fun asString() = "[ " + list.joinToString() + " ]"
    override fun asBoolean() = true
    override fun asFunction() = arrayListOf<FunctionOverload>()

    override fun toString() = asString()
    override fun getType() = BailaType("List")
    override fun getDefaultValue() = ListValue()
}