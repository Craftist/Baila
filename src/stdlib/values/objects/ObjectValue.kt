package stdlib.values.objects

import stdlib.NameTable
import stdlib.libstruct.functions.FunctionOverload
import stdlib.values.Value

class ObjectValue(val objectHeapIndex: Int?) : Value() {
    override fun asNumber() = -1.0
    override fun asString() = if (objectHeapIndex == null) "null" else "[object ${NameTable.getHeapObject(objectHeapIndex).getType()}]"
    override fun asBoolean() = false
    override fun asFunction() = arrayListOf<FunctionOverload>()

    override fun toString() = asString()

    override fun getType() = NameTable.getHeapObject(objectHeapIndex ?: -1).getType() //Type("Object")

    override fun getDefaultValue() = ObjectValue(null)
}