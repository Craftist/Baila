package parser.ast.expressions

import parser.ast.errors.TypeError
import stdlib.NameTable
import stdlib.libstruct.Field
import stdlib.libstruct.BailaType
import stdlib.values.Value
import stdlib.values.objects.ObjectValue

class ObjectDotSetExpression(val valueExpr: Expression, val fieldIdent: String, val newValue: Expression) : Expression {
    override fun eval(): Value {
        // Get next element in object chain
        val v = valueExpr.eval()
        if (v is ObjectValue) {
            val vHeapIndex = v.objectHeapIndex ?: throw TypeError.nullReference(fieldIdent)
            val obj = NameTable.getHeapObject(vHeapIndex)
            val field = obj.getInstanceMember(fieldIdent) ?: obj.getStaticMember(fieldIdent) ?: throw TypeError.fieldIsUndefined(fieldIdent, obj.getType().className)

            if (field is Field) {
                if (field.readonly) {
                    throw TypeError.fieldIsConstant(valueExpr, fieldIdent)
                }
            } else {
                throw Exception("This type of ClassMember is not supported yet: ${field::class.simpleName}")
            }

            field.value = newValue.eval()
            return field.value ?: throw Exception("ObjectDotSetExpression:20, error ID=acokasjcasc09akdakosackx90sckas09cijkasd0c9as0kc (search in files)")
        }
        throw TypeError.cannotConvert(v.getType(), BailaType("Object"))
    }

    override fun toString(): String {
        return "ObjectDotSetExpression($valueExpr, $fieldIdent)"
    }

    override fun asCodeRepr(): String = "${valueExpr.asCodeRepr()}.$fieldIdent = ${newValue.asCodeRepr()}"
}