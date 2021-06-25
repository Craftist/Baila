package stdlib.values.classes

import parser.tokenizer.TokenType
import stdlib.libstruct.*
import stdlib.libstruct.functions.Callable
import stdlib.libstruct.functions.FunctionOverload
import stdlib.values.primitives.StringValue
import kotlin.String

class String : Class("String") {
    override fun getConstructor(): ArrayList<FunctionOverload> = super.getConstructor()

    override fun getInstanceMember(memberName: String): ClassMember? {
        if (memberName == "empty") {
            return Field(Accessibility.Public, true, StringValue(""))
        }
        return null
    }

    override fun getStaticMember(memberName: String): ClassMember? {
        if (memberName == "hello") {
            return Field(Accessibility.Public, false, StringValue("Hello"))
        }
        return null
    }
}