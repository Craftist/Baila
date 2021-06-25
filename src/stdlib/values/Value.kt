package stdlib.values

import parser.ast.errors.TypeError
import parser.tokenizer.TokenType
import stdlib.NameTable
import stdlib.libstruct.BailaType
import stdlib.libstruct.TypeContainer
import stdlib.libstruct.functions.FunctionOverload
import stdlib.values.objects.ObjectValue
import stdlib.values.primitives.BooleanValue
import stdlib.values.primitives.NumberValue
import stdlib.values.primitives.StringValue
import kotlin.math.pow

inline fun <reified T : TypeContainer> Value.asObject(): T {
    if (this !is ObjectValue) {
        throw Exception("Attempt to convert ${this::class.simpleName} to Object ${T::class.simpleName}")
    }

    val heapIndex = this.objectHeapIndex ?: throw Exception("Got null access while trying to call Value.asObject()")
    return NameTable.getObjectFromHeap(heapIndex)
}

abstract class Value {
    abstract fun asNumber() : Double
    abstract fun asString() : String
    abstract fun asBoolean() : Boolean
    abstract fun asFunction() : ArrayList<FunctionOverload>

    abstract fun getType() : BailaType
    abstract fun getDefaultValue() : Value

    companion object {
        fun add(left: Value, right: Value) : Value {
            if (left is NumberValue && right is NumberValue)
                return NumberValue(left.asNumber() + right.asNumber())

            if (left is StringValue || right is StringValue)
                return StringValue(left.asString() + right.asString())

            if (left is ObjectValue) {
                val leftType = NameTable.getHeapObject(left.objectHeapIndex ?: -1)
                val operator = leftType.getBinaryOperator(TokenType.Plus)

                if (operator != null && operator.overloadExists(listOf(right))) {
                    return operator.call(null, listOf(right))
                }
            }

            throw TypeError("Cannot add '%s' and '%s'".format(left.getType(), right.getType()))
        }

        fun sub(left: Value, right: Value) : Value {
            if (left is NumberValue && right is NumberValue)
                return NumberValue(left.asNumber() - right.asNumber())

            if (left is ObjectValue) {
                val leftType = NameTable.getHeapObject(left.objectHeapIndex ?: -1)
                val operator = leftType.getBinaryOperator(TokenType.Minus)

                if (operator != null && operator.overloadExists(listOf(right))) {
                    return operator.call(null, listOf(right))
                }
            }

            throw TypeError("Cannot subtract '%s' and '%s'".format(left.getType(), right.getType()))
        }

        fun mul(left: Value, right: Value) : Value {
            if (left is NumberValue && right is NumberValue)
                return NumberValue(left.asNumber() * right.asNumber())

            if (left is StringValue && right is NumberValue) {
                val r = right.asNumber()
                if (r < 1) {
                    val s = left.asString()
                    return StringValue(s.substring(0, (r * s.length).toInt()))
                }
                return StringValue(left.asString().repeat(r.toInt()))
            }

            if (left is NumberValue && right is StringValue) {
                val l = left.asNumber()
                if (l < 1) {
                    val s = right.asString()
                    return StringValue(s.substring(0, (l * s.length).toInt()))
                }
                return StringValue(right.asString().repeat(l.toInt()))
            }

            if (left is ObjectValue) {
                val leftType = NameTable.getHeapObject(left.objectHeapIndex ?: -1)
                val operator = leftType.getBinaryOperator(TokenType.Star)

                if (operator != null && operator.overloadExists(listOf(right))) {
                    return operator.call(null, listOf(right))
                }
            }

            throw TypeError("Cannot multiply '%s' and '%s'".format(left.getType(), right.getType()))
        }

        fun div(left: Value, right: Value) : Value {
            if (left is NumberValue && right is NumberValue)
                return NumberValue(left.asNumber() / right.asNumber())

            if (left is ObjectValue) {
                val leftType = NameTable.getHeapObject(left.objectHeapIndex ?: -1)
                val operator = leftType.getBinaryOperator(TokenType.Slash)

                if (operator != null && operator.overloadExists(listOf(right))) {
                    return operator.call(null, listOf(right))
                }
            }

            throw TypeError("Cannot divide '%s' and '%s'".format(left.getType(), right.getType()))
        }

        fun rem(left: Value, right: Value) : Value {
            if (left is NumberValue && right is NumberValue)
                return NumberValue(left.asNumber() % right.asNumber())

            if (left is ObjectValue) {
                val leftType = NameTable.getHeapObject(left.objectHeapIndex ?: -1)
                val operator = leftType.getBinaryOperator(TokenType.Percent)

                if (operator != null && operator.overloadExists(listOf(right))) {
                    return operator.call(null, listOf(right))
                }
            }

            throw TypeError("Cannot get remainder of '%s' and '%s'".format(left.getType(), right.getType()))
        }

        fun pow(left: Value, right: Value) : Value {
            if (left is NumberValue && right is NumberValue)
                return NumberValue(left.asNumber().pow(right.asNumber()))

            if (left is ObjectValue) {
                val leftType = NameTable.getHeapObject(left.objectHeapIndex ?: -1)
                val operator = leftType.getBinaryOperator(TokenType.StarStar)

                if (operator != null && operator.overloadExists(listOf(right))) {
                    return operator.call(null, listOf(right))
                }
            }

            throw TypeError("Cannot exponentiate '%s' and '%s'".format(left.getType(), right.getType()))
        }

        fun intdiv(left: Value, right: Value) : Value {
            if (left is NumberValue && right is NumberValue)
                return NumberValue((left.asNumber() / right.asNumber()).toInt().toDouble())

            if (left is ObjectValue) {
                val leftType = NameTable.getHeapObject(left.objectHeapIndex ?: -1)
                val operator = leftType.getBinaryOperator(TokenType.SlashSlash)

                if (operator != null && operator.overloadExists(listOf(right))) {
                    return operator.call(null, listOf(right))
                }
            }

            throw TypeError("Cannot intdiv '%s' and '%s'".format(left.getType(), right.getType()))
        }

        fun band(left: Value, right: Value) : Value {
            if (left is NumberValue && right is NumberValue)
                return NumberValue(left.asNumber().toInt().and(right.asNumber().toInt()).toDouble())

            if (left is ObjectValue) {
                val leftType = NameTable.getHeapObject(left.objectHeapIndex ?: -1)
                val operator = leftType.getBinaryOperator(TokenType.AmpAmp)

                if (operator != null && operator.overloadExists(listOf(right))) {
                    return operator.call(null, listOf(right))
                }
            }

            throw TypeError("Cannot perform bitwise conjunction on '%s' and '%s'".format(left.getType(), right.getType()))
        }

        fun bor(left: Value, right: Value) : Value {
            if (left is NumberValue && right is NumberValue)
                return NumberValue(left.asNumber().toInt().or(right.asNumber().toInt()).toDouble())

            if (left is ObjectValue) {
                val leftType = NameTable.getHeapObject(left.objectHeapIndex ?: -1)
                val operator = leftType.getBinaryOperator(TokenType.BarBar)

                if (operator != null && operator.overloadExists(listOf(right))) {
                    return operator.call(null, listOf(right))
                }
            }

            throw TypeError("Cannot perform bitwise disjunction on '%s' and '%s'".format(left.getType(), right.getType()))
        }

        fun bxor(left: Value, right: Value) : Value {
            if (left is NumberValue && right is NumberValue)
                return NumberValue(left.asNumber().toInt().xor(right.asNumber().toInt()).toDouble())

            throw TypeError("Cannot perform bitwise exclusive disjunction on '%s' and '%s'".format(left.getType(), right.getType()))
        }

        fun eq(left: Value, right: Value) : Value {
            if (left is ObjectValue) {
                val leftType = NameTable.getHeapObject(left.objectHeapIndex ?: -1)
                val operator = leftType.getBinaryOperator(TokenType.EqEq)

                if (operator != null && operator.overloadExists(listOf(right))) {
                    return operator.call(null, listOf(right))
                }
            }

            return BooleanValue(left.getType() == right.getType() && left.asString() == right.asString())
        }

        fun neq(left: Value, right: Value) : Value {
            if (left is ObjectValue) {
                val leftType = NameTable.getHeapObject(left.objectHeapIndex ?: -1)
                val operator = leftType.getBinaryOperator(TokenType.ExclEq)

                if (operator != null && operator.overloadExists(listOf(right))) {
                    return operator.call(null, listOf(right))
                }
            }

            return BooleanValue(left.getType() != right.getType() || left.asString() != right.asString())
        }

        fun lt(left: Value, right: Value) : Value {
            if (left is NumberValue && right is NumberValue)
                return BooleanValue(left.asNumber().toInt() < right.asNumber().toInt())
            if (left is StringValue && right is StringValue)
                return BooleanValue(left.asString() < right.asString())

            throw TypeError("Cannot compare '%s' and '%s'".format(left.getType(), right.getType()))
        }

        fun le(left: Value, right: Value) : Value {
            if (left is NumberValue && right is NumberValue)
                return BooleanValue(left.asNumber().toInt() <= right.asNumber().toInt())
            if (left is StringValue && right is StringValue)
                return BooleanValue(left.asString() <= right.asString())

            throw TypeError("Cannot compare '%s' and '%s'".format(left.getType(), right.getType()))
        }

        fun gt(left: Value, right: Value) : Value {
            if (left is NumberValue && right is NumberValue)
                return BooleanValue(left.asNumber().toInt() > right.asNumber().toInt())
            if (left is StringValue && right is StringValue)
                return BooleanValue(left.asString() > right.asString())

            throw TypeError("Cannot compare '%s' and '%s'".format(left.getType(), right.getType()))
        }

        fun ge(left: Value, right: Value) : Value {
            if (left is NumberValue && right is NumberValue)
                return BooleanValue(left.asNumber().toInt() >= right.asNumber().toInt())
            if (left is StringValue && right is StringValue)
                return BooleanValue(left.asString() >= right.asString())

            throw TypeError("Cannot compare '%s' and '%s'".format(left.getType(), right.getType()))
        }
    }
}