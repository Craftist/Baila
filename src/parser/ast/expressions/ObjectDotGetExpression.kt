package parser.ast.expressions

import parser.ast.errors.TypeError
import stdlib.NameTable
import stdlib.libstruct.BailaType
import stdlib.values.Value
import stdlib.values.objects.ObjectValue

class ObjectDotGetExpression(val valueExpr: Expression, val fieldIdent: String) : Expression {
    override fun eval(): Value {
        // Get next element in object chain
        val v = valueExpr.eval()
        if (v is ObjectValue) {
            val vHeapIndex = v.objectHeapIndex ?: throw TypeError.nullReference(fieldIdent)
            val obj = NameTable.getHeapObject(vHeapIndex)

            if (obj is stdlib.values.classes.Type) {
                // Static context, e.g. ClassName.propName

                val staticField = obj.correspondingTypeContainer.getStaticMember(fieldIdent)
                    ?: throw TypeError.fieldIsUndefined(fieldIdent, obj.getType().className)

                return staticField.value ?: throw Exception("Error in ObjectDotGetExpression:25, error ID = iujchsiuhaciusdhf879asfy9ashfiuashf8ahsf89ashdshfuoishdfoi")
            } else {
                // Instance context, e.g. instanceName.propName

                val instanceOrStaticField = obj.getInstanceMember(fieldIdent)
                    ?: obj.getStaticMember(fieldIdent)
                    ?: throw TypeError.fieldIsUndefined(fieldIdent, obj.getType().className)

                return instanceOrStaticField.value ?: throw Exception("Error in ObjectDotGetExpression:33, error ID = jsdf98dhjfg09sjdfokslsf89wsojfqfokewnfowsfmndkfnmasof")
            }
        }
        throw TypeError.cannotConvert(v.getType(), BailaType("Object"))
    }

    override fun toString(): String {
        return "ObjectDotGetExpression($valueExpr, $fieldIdent)"
    }

    override fun asCodeRepr(): String = "${valueExpr.asCodeRepr()}.${fieldIdent}"
}