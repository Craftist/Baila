package stdlib.libstruct.functions

import parser.ast.errors.TypeError
import parser.ast.expressions.Expression
import stdlib.NameTable
import stdlib.libstruct.BailaType
import stdlib.libstruct.TypeContainer
import stdlib.values.Value
import stdlib.values.objects.ObjectValue
import kotlin.reflect.full.createInstance

/**
 * Represents one particular overload of a function. A function (i.e. the name bearer) can have multiple overloads,
 * this class represents the overload signature and code (body).
 * @param callback Callable of this function overload.
 * @param parameters Parameter list.
 * @param returnType If it's null, then the function is void.
 */
data class FunctionOverload(val callback: Callable, val parameters: ArrayList<Parameter>, val returnType: BailaType?) {
    constructor(parameters: ArrayList<Parameter>, returnType: BailaType?, function: (args: Array<Value>) -> Value?)
            : this(object : Callable() { override fun call(args: Array<Value>): Value? = function(args) }, parameters, returnType)

    // Void
    constructor(parameters: ArrayList<Parameter>, function: (args: Array<Value>) -> Unit)
            : this(object : Callable() { override fun call(args: Array<Value>): Value? { function(args); return null } }, parameters, null)

    fun compareTo(other: FunctionOverload): Boolean {
        // we are comparing only by parameter types
        return parameters.filter { it.defaultValue == null }.map { it.type } as ArrayList<BailaType> == other.parameters.map { it.type } as ArrayList<BailaType>
    }

    fun compareTo(other: ArrayList<BailaType>): Boolean {
        return parameters.filter { it.defaultValue == null }.map { it.type } as ArrayList<BailaType> == other
    }

    companion object {
        fun empty() = FunctionOverload(Callable.emptyFunc, arrayListOf(), null)

        fun constructor(type: TypeContainer, parameters: ArrayList<Parameter>, callback: (args: Array<Value>) -> Unit): FunctionOverload {
            return FunctionOverload(
                object : Callable() {
                    override fun call(args: Array<Value>): Value {
                        callback(args)
                        val newObject = NameTable.createHeapObject(type::class.createInstance())
                        return ObjectValue(newObject)
                    }
                },
                parameters,
                type.getType()
                //type.getType()
            )
        }
    }
}

data class FunctionWithOverloads(val overloads: ArrayList<FunctionOverload> = arrayListOf()) {
    fun overloadExists(args: List<Value>) : Boolean {
        val found: ArrayList<FunctionOverload> = getOverloads(args)
        return found.size > 0
    }

    fun getOverloads(args: List<Value>): ArrayList<FunctionOverload> {
        val argTypes = args.map { it.getType() }
        val found: ArrayList<FunctionOverload> = arrayListOf()

        for (i in overloads) {
            // if we passed less arguments than the required parameters count, skip that overload
            if (argTypes.size < i.parameters.filter { it.defaultValue == null }.size) {
                continue
            }

            // if we passed more arguments than the overall parameters count, skip that overload
            if (argTypes.size > i.parameters.size) {
                continue
            }

            //Debug.log("kok = ${i.parameters.filter { it.defaultValue == null }}")
            val slicedTypes = i.parameters/*.filter { it.defaultValue == null }*/.slice(0 until argTypes.size).map { it.type }

            if (i.parameters.map { it.type } == argTypes) {
                // best match
                found.add(i)
                break
            }

            if (i.parameters.size >= argTypes.size && slicedTypes == argTypes) {
                found.add(i)
            }
        }
        return found
    }

    @JvmName("callWithExpressions")
    fun call(holder: Expression?, args: List<Expression>): Value {
        return call(holder, args.map { it.eval() })
    }

    @JvmName("callWithValues")
    fun call(holder: Expression?, args: List<Value>): Value {
        val found = getOverloads(args)
        val argTypes = args.map { it.getType() }

        if (found.size == 0) {
            throw TypeError.unableToFindOverload(argTypes, holder?.asCodeRepr() ?: "<holder not found :: error id = 208c95n675cn956n345t6c47ry34fdwedu>")
        }

        if (found.size > 1) {
            throw Exception("Ambiguous overload matching: found ${found.size} overloads for ${argTypes.size} argument(s)${if (argTypes.isEmpty()) "" else " of types " + argTypes.joinToString(", ")}")
        }

        val sufficientOverload = found[0]

        NameTable.pushScope()

        for (i in 0 until sufficientOverload.parameters.size) {
            val currentParameter = sufficientOverload.parameters[i]

            val parameterVarName = currentParameter.name

            //Debug.log("parameterVarName=$parameterVarName, i=$i, args.size=${args.size}, pars size=${sufficientOverload.parameters.size}")
            val parameterValue = if (args.size <= i) {
                currentParameter.defaultValue?.eval()
                    ?: throw Exception("No default value provided for parameter '$parameterVarName'")
            } else args[i]

            Debug.log("Added argument $parameterVarName=$parameterValue")
            NameTable.add(parameterVarName, currentParameter.type, parameterValue)
        }

        val functionCallResult = sufficientOverload.callback.call(args.toTypedArray())
        val returnValue = functionCallResult
            ?: sufficientOverload.returnType?.getDefaultValue()

        NameTable.popScope()

        if (sufficientOverload.returnType == null) {
            // if return type of the function is not specified, just return what's needed. Type check has been passed.
            return returnValue ?: ObjectValue(null) // maybe return VoidValue() or something along those lines

            // FIXED !!!!!!!!!!!!!!!!!!! fix TestClass().greet() not working despite (TestClass()).greet() works (issue in Parser#primary)
            // TODO !!!!!!!!!!!!!!!!!!! add ability to set props and indexes like list[3] = value, obj.prop = value (with type checking of course, it's not js)
            // TODO !!!!!!!!!!!!!!!!!!! fix functions: "function g() { println('a') }" ==> SyntaxError: Unexpected token <EOF> on }
        }

        if (returnValue == null) {
            throw Exception("Unknown return value of unknown return type")
        }

        if (!BailaType.compareTypesContravariant(returnValue.getType(), sufficientOverload.returnType)) {
            throw Exception(
                "Cannot convert returned value of type '%s' to the return type '%s' of the function".format(
                    returnValue.getType(),
                    sufficientOverload.returnType
                )
            )
        }

        return returnValue
    }
}