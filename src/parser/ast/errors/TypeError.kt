package parser.ast.errors

import parser.ast.expressions.Expression
import stdlib.libstruct.BailaType

class TypeError constructor(override val message: String?) : Exception(message)
{
    companion object {
        fun fieldIsConstant(objectExpression: Expression, fieldName: String) : TypeError {
            return TypeError("Cannot reassign ${objectExpression.asCodeRepr()}.${fieldName}: field is constant")
        }

        fun typeCannotHaveDefaultValue(type: BailaType) : TypeError {
            return TypeError("Type $type cannot have default value")
        }

        fun unableToFindOverload(argTypes: List<BailaType>, funcName: String) : TypeError {
            return TypeError("$funcName(): Unable to find overload with ${argTypes.size} parameter(s)${if (argTypes.isEmpty()) "" else ": " + argTypes.joinToString(", ")}")
        }

        fun cannotConvert(originalType: BailaType, destinationType: BailaType) : TypeError {
            return TypeError("Cannot convert $originalType type to $destinationType")
        }

        fun nullReference(propName: String) : TypeError {
            return TypeError("Cannot access '$propName' of null")
        }

        fun fieldIsUndefined(fieldName: String) : TypeError {
            return TypeError("Field '$fieldName' is undefined")
        }

        fun fieldIsUndefined(fieldName: String, typeName: String) : TypeError {
            return TypeError("Field '$fieldName' is undefined in type '$typeName'")
        }

        fun isNotClass(typeName: String): TypeError {
            return TypeError("Type '$typeName' is not a class")
        }

        fun isNotStruct(typeName: String): TypeError {
            return TypeError("Type '$typeName' is not a struct")
        }

        fun objectIsNotCallable(objectRepr: String): TypeError {
            return TypeError("Object $objectRepr is not callable")
        }
    }
}