package stdlib

import parser.ast.errors.ReferenceError
import parser.ast.errors.TypeError
import stdlib.libstruct.Class
import stdlib.libstruct.Struct
import stdlib.libstruct.functions.Callable
import stdlib.libstruct.functions.FunctionOverload
import stdlib.libstruct.BailaType
import stdlib.libstruct.TypeContainer
import stdlib.libstruct.functions.toParameter
import stdlib.values.*
import stdlib.values.objects.ObjectValue
// import stdlib.values.objects.TypeValue
import stdlib.values.primitives.FunctionValue
import stdlib.values.primitives.NumberValue
import java.util.*


class NameTableMember(var type: BailaType, var value: Value, var immutable: Boolean = false)

class Scope(val parentScope: Scope? = null) {
    val members = TreeMap<String, NameTableMember>()

    /**
     * Adds a variable with the specified type and, optionally, with a value.
     */
    fun addVariable(name: String, type: BailaType, value: Value = ObjectValue(null)) {
        if (exists(name)) {
            if (!BailaType.compareTypesStrict(getVariable(name).type, type)) {
                throw Exception("Cannot convert type %s to the type %s of the variable %s".format(type, getVariable(name).type, name))
            }
        }
        members[name] = NameTableMember(type, value)
    }

    /**
     * Inferts the type of the variable from the type of the value.
     */
    fun addVariableInferred(name: String, value: Value) {
        addVariable(name, value.getType(), value)
    }

    /**
     * Adds a constant with the specified value and the type inferred from the value.
     */
    fun addConstant(name: String, value: Value = ObjectValue(null)) {
        if (exists(name)) {
            throw Exception("Constant '%s' is already defined".format(name))
        }
        members[name] = NameTableMember(value.getType(), value, true)
    }

    fun setVariable(name: String, value: Value) {
        val _var = getVariable(name)

        // constant check
        if (_var.immutable) {
            throw Exception("Cannot redefine constant '%s'".format(name))
        }

        /*
        type check
        type checking is covariant, i.e. variable allows to hold its own value type, as well as all that were inherited from it.
        class SuperBase {
          public func meow { println("Meow") }
        }
        class Base : SuperBase {
          public func woof { println("Woof") }
        }
        class Sub : Base {
          public func bark { println("Bark") }
        }
        var typeBase: Base = Base()
        typeBase.woof() # OK
        typeBase.meow() # OK, meow was inherited from SuperCase
        typeBase = Sub() # ok, since all the base members are defined in Sub (i.e. inherited)
        typeBase.bark() # OK
        typeBase = SuperBase() # bad, since Base could've defined some members that are not present in SuperBase and
        typeBase.woof() # ERROR, typeBase has type of Base, but woof method is not defined on its actual type (SuperBase)
        thus accessing those members could result in not finding them.
        */
        if (!BailaType.compareTypesCovariant(getVariable(name).type, value.getType())) {
            throw Exception("'%s' type cannot be stored inside of variable '%s' with type '%s'".format(value.getType(), name, _var.type))
        }

        _var.value = value
    }

    fun getVariable(name: String) : NameTableMember {
        return members[name] ?: throw Exception("'%s' is not defined".format(name))
    }

    /**
     * Returns true, if the variable exists, false otherwise.
     */
    fun exists(name: String) : Boolean {
        return members.containsKey(name)
    }
}

private class ScopeFindData(var isFound: Boolean = false, var scope: Scope? = null)

object NameTable {
    var scope = Scope()

    val heap    = TreeMap<Int, TypeContainer>()
    var heapIndex = 0

    inline fun <reified T : TypeContainer> getObjectFromHeap(heapIndex: Int) : T {
        val obj = heap[heapIndex] ?: throw Exception("Object with heapIndex=$heapIndex not found. Error ID = s9hsuhsiuohclasjknfdaw8ualdja089f7daw8cuyassy7godf7")

        if (obj !is T) {
            throw Exception("Could not convert object with heapIndex=$heapIndex to ${T::class.simpleName}")
        }

        return obj
    }

    init {
        // Built-in members and constants are here, as well as types.
        scope.addVariableInferred("test", NumberValue(123.0))
        scope.addVariableInferred("print", FunctionValue.withOverloads(
                FunctionOverload(object : Callable() {
                    override fun call(args: Array<Value>): Value? {
                        print(args[0].asString())
                        return null
                    }
                }, arrayListOf("text : String".toParameter()), BailaType("String")),
                FunctionOverload(object : Callable() {
                    override fun call(args: Array<Value>): Value? {
                        print(args[0].asString())
                        return null
                    }
                }, arrayListOf("text : Number".toParameter()), BailaType("String"))
            )
        )
        scope.addVariableInferred("println", FunctionValue.withOverloads(
                FunctionOverload(object : Callable() {
                    override fun call(args: Array<Value>): Value? {
                        println(args[0].asString())
                        return null
                    }
                }, arrayListOf("text : String".toParameter()), BailaType("String")),
                FunctionOverload(object : Callable() {
                    override fun call(args: Array<Value>): Value? {
                        println(args[0].asString())
                        return null
                    }
                }, arrayListOf("text : Number".toParameter()), BailaType("String"))
            )
        )
        //scope.addVariableInferred("TestType", TypeValue(stdlib.libstruct.Class(Type("TestType"))))
        scope.addConstant("Object", ObjectValue(createHeapObject(
                stdlib.values.classes.Type(stdlib.values.classes.Object())
        )))
        scope.addConstant("TestClass", ObjectValue(createHeapObject(
                stdlib.values.classes.Type(stdlib.values.classes.TestClass())
        )))
        scope.addConstant("String", ObjectValue(createHeapObject(
                stdlib.values.classes.Type(stdlib.values.classes.String())
        )))
    }

    /**
     * Returns the current nametable instance.
     */
    fun get(name: String) : NameTableMember {
        val scopeData = findScope(name)
        if (scopeData.isFound) {
            return scopeData.scope?.getVariable(name) ?: throw ReferenceError.isNotDefined(name)
        }
        throw ReferenceError.isNotDefined(name)
    }

    fun getHeapObject(heapIndex: Int) : TypeContainer {
        return heap[heapIndex] ?: throw Exception("Object by index %s is not on heap".format(heapIndex))
    }

    fun getClass(name: String) : Class {
        val heapIndex = (get(name).value as ObjectValue).objectHeapIndex ?: throw TypeError.nullReference(name)
        val obj = getHeapObject(heapIndex)
        return (obj as? Class) ?: throw TypeError.isNotClass(name)
    }

    fun getStruct(name: String) : Struct {
        val heapIndex = (get(name).value as ObjectValue).objectHeapIndex ?: throw TypeError.nullReference(name)
        val obj = getHeapObject(heapIndex)
        return (obj as? Struct) ?: throw TypeError.isNotStruct(name)
    }

    fun add(name: String, type: BailaType, value: Value = ObjectValue(null)) {
        scope.addVariable(name, type, value)
        //findScope(name).scope?.addVariable(name, type, value)
    }

    /**
     * Inferts the type of the variable from the type of the value.
     */
    fun addInferred(name: String, value: Value) {
        add(name, value.getType(), value)
    }

    /**
     * Adds a constant with the specified value and the type inferred from the value.
     */
    fun addConstant(name: String, value: Value = ObjectValue(null)) {
        findScope(name).scope?.addConstant(name, value)
    }

    fun set(name: String, value: Value) {
        findScope(name).scope?.setVariable(name, value)
    }

    /**
     * Puts the specified object into the heap and returns its index in the heap.
     * @param obj The specified object.
     * @return Index of the object in the heap.
     */
    fun createHeapObject(obj: TypeContainer) : Int {
        heap[heapIndex] = obj
        return heapIndex++
    }

    private fun findScope(varName: String) : ScopeFindData {
        val result = ScopeFindData()

        var current: Scope? = scope
        do {
            if (current?.exists(varName) != false) {
                result.isFound = true
                result.scope = current
                return result
            }
            current = current.parentScope
        } while (current != null)

        result.isFound = false
        result.scope = scope
        return result
    }

    fun exists(varName: String) : Boolean {
        return findScope(varName).isFound
    }

    /**
     * Pushes into the nametable stack.
     * Used in block evaluation and function execution.
     */
    fun pushScope() {
        scope = Scope(scope)
    }

    /**
     * Pops out of the nametable stack, removing members created after the last pushing.
     */
    fun popScope() {
        val parent = scope.parentScope
        if (parent != null) {
            scope = parent
        }
    }
}