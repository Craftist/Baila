package stdlib.libstruct

import parser.tokenizer.TokenType
import stdlib.libstruct.functions.FunctionOverload
import stdlib.libstruct.functions.FunctionWithOverloads
import stdlib.values.Value
import stdlib.values.objects.ObjectValue

/**
 * Represents an abstract type container, such as a class, a struct or an enum type
 */
abstract class TypeContainer {
    fun createInstance(vararg args: Value): ObjectValue {
        return FunctionWithOverloads(m_constructor).call(null, args.toList()) as ObjectValue
    }

    protected var m_constructor: ArrayList<FunctionOverload> = arrayListOf()
    protected var m_instanceMembers = HashMap<String, ClassMember>() // methods and subclasses are here too
    protected var m_staticMembers = HashMap<String, ClassMember>()
    protected var m_operators = OperatorOverloadTable()

    fun get(memberName: String): ClassMember =
        getInstanceMember(memberName)
            ?: getStaticMember(memberName)
            ?: throw Exception("Member doesn't exist: $memberName, in ${this::class.simpleName}")

    /**
     * Returns null when the required constructor does not exist
     */
    open fun getConstructor(): ArrayList<FunctionOverload> {
        return m_constructor
    }

    /**
     * Returns null when the required instance member does not exist
     */
    open fun getInstanceMember(memberName: String): ClassMember? {
        return m_instanceMembers[memberName]
    }

    /**
     * Returns null when the required static member does not exist
     */
    open fun getStaticMember(memberName: String): ClassMember? {
        return m_staticMembers[memberName]
    }

    /**
     * Returns null when the required operator overload does not exist
     */
    open fun getUnaryOperator(operator: TokenType): FunctionWithOverloads? {
        return m_operators.unary[operator]
    }

    /**
     * Returns null when the required operator overload does not exist
     */
    open fun getBinaryOperator(operator: TokenType): FunctionWithOverloads? {
        return m_operators.binary[operator]
    }

    /**
     * Returns the type itself
     */
    abstract fun getType(): BailaType
}