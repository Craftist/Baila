package stdlib.libstruct

import stdlib.NameTable
import stdlib.values.primitives.BooleanValue
import stdlib.values.primitives.NumberValue
import stdlib.values.objects.ObjectValue
import stdlib.values.Value
import stdlib.values.primitives.StringValue
import java.lang.StringBuilder

data class BailaType(val className: String, val nullable: Boolean = false, val generics: ArrayList<BailaType> = ArrayList()) {
    fun addGeneric(generic: BailaType) {
        generics.add(generic)
    }

    override fun toString(): String {
        val genericsSb = StringBuilder()

        if (generics.size > 0) {
            genericsSb.append('<')
            genericsSb.append(generics.joinToString { it.toString() })
            genericsSb.append('>')
        }

        return (if (nullable) "?" else "") + genericsSb.toString() + className
    }

    override operator fun equals(other: Any?): Boolean {
        if (other is BailaType) {
            return className == other.className && nullable == other.nullable && generics == other.generics && toString() == other.toString()
        }
        return false
    }

    override fun hashCode(): Int {
        return (37L * generics.hashCode() + 367 * className.hashCode() + 431 * nullable.hashCode()).mod(Int.MAX_VALUE)
    }

    // =========================================================

    fun getDefaultValue() : Value {
        // Find primitive
        when (className) {
            "String" -> return StringValue("")
            "Boolean" -> return BooleanValue(false)
            "Number" -> return NumberValue(0.0)
            "Object" -> return ObjectValue(null)
        }

        // If not found in primitives, search NameTable for the type
        if (NameTable.exists(className)) {
            return ObjectValue(null)
        }

        throw Exception("Error: unknown className '%s' given for Type.getDefaultValue()".format(className))
    }

    // =========================================================

    companion object {
        class Builtin {
            companion object {
                val String = BailaType("String")
                val Number = BailaType("Number")
                val Boolean = BailaType("Boolean")
                val Object = BailaType("Object")
                
            }
        }

        /**
         * Strictly compares the type of the variable to the given type.
         * If the variable doesn't exist, returns false.
         */
        fun compareTypesStrict(type1: BailaType, type2: BailaType) : Boolean = type1 == type2

        /**
         * Covariantly compares the child type to the parent type.
         * It returns true for all types that this child type inherits, including itself.
         * That is if the child inherits from Object, then parent=Object will return true.
         */
        fun compareTypesCovariant(child: BailaType, parent: BailaType) : Boolean {
            if (child == parent) return true

            var childParentClass = NameTable.getClass(NameTable.getClass(child.className).inheritingType ?: return false)
            val parentClass = NameTable.getClass(parent.className)
            while (childParentClass != parentClass) {
                if (childParentClass.inheritingType == null) {
                    break
                }

                childParentClass = NameTable.getClass(childParentClass.inheritingType!!)
            }

            return childParentClass == parentClass
        }

        /**
         * Contravariantly compares the type of the variable to the given type.
         * It returns true for all types that were inherited from the variable type, including itself.
         * That is if B inherits from A, and variable type is A, then type=B will return here true.
         * If the variable doesn't exist, returns false.
         */
        fun compareTypesContravariant(parent: BailaType, child: BailaType) : Boolean = compareTypesCovariant(parent, child)
    }
}