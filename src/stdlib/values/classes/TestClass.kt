package stdlib.values.classes

import parser.tokenizer.TokenType
import stdlib.libstruct.*
import stdlib.libstruct.annotations.BailaField
import stdlib.libstruct.functions.*
import stdlib.values.Value
import stdlib.values.asObject
import stdlib.values.primitives.FunctionValue
import stdlib.values.primitives.NumberValue
import stdlib.values.primitives.StringValue
import kotlin.reflect.KVisibility
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.starProjectedType

class TestClass : Class("TestClass") {
    init {
        m_constructor = arrayListOf(
            FunctionOverload.constructor(this, arrayListOf()) {
                //println("TestClass ctor() called")
            },
            FunctionOverload.constructor(this, arrayListOf("x: Number".toParameter())) {
                //println("TestClass ctor(Number) called")
            },
            FunctionOverload.constructor(this, arrayListOf("x: String".toParameter())) {
                //println("TestClass ctor(String) called")
            }
        )

        m_staticMembers["testStaticMember"] = Field(Accessibility.Public, true, StringValue("Pizdec huy nahuy"))

        m_operators.binary[TokenType.Plus] = FunctionWithOverloads(arrayListOf(
            FunctionOverload(arrayListOf("other: TestClass".toParameter()), BailaType("TestClass")) { args ->
                this.createInstance().also { retInstance ->
                    retInstance.asObject<TestClass>().get("testNumVar").value = Value.add(
                        this.get("testNumVar").value!!,
                        args[0].asObject<TestClass>().get("testNumVar").value ?: throw Exception("Got null. Error ID = hfafhqy0fduwe9hd0wqpdyiuhagsc7yhqwadehw7d0phqwe7ydc0hwqaxa0cdyf7fep9")
                    )
                }
            },

            FunctionOverload(arrayListOf("other: Number".toParameter()), BailaType("TestClass")) { args ->
                this.createInstance().also { retInstance ->
                    retInstance.asObject<TestClass>().get("testNumVar").value = Value.add(this.get("testNumVar").value!!, args[0])
                }
            }
        ))

        m_instanceMembers["testStrVar"] = Field(Accessibility.Public, false, StringValue("Hello"))
        m_instanceMembers["testStrVal"] = Field(Accessibility.Public, true, StringValue("Hello 2"))
        m_instanceMembers["testNumVar"] = Field(Accessibility.Public, false, NumberValue(123.0))
        m_instanceMembers["testNumVal"] = Field(Accessibility.Public, true, NumberValue(456.0))
        m_instanceMembers["greet"] = Field(Accessibility.Public, true, FunctionValue.withOverloads(
            FunctionOverload(arrayListOf()) { println("Hello, world!") },
            FunctionOverload(arrayListOf("who: String".toParameter())) { args -> println("Hello, ${args[0].asString()}!") }
        ))

        val annotatedProperties = this::class.memberProperties.filter { it.hasAnnotation<BailaField>() }
        Debug.log("TypeContainer :: There are ${annotatedProperties.size} annotatedProperties in ${this::class.simpleName}")

        for (property in annotatedProperties) {
            if (property.returnType.isSubtypeOf(Value::class.starProjectedType)) {
                val accessibility = when (property.visibility) {
                    KVisibility.PUBLIC -> Accessibility.Public
                    KVisibility.PROTECTED -> Accessibility.Protected
                    else -> Accessibility.Private
                }

                val isReadonly = property.isFinal
                val fieldDefaultValue = property.getter.call(this) as Value

                Debug.log("Creating field with name '${property.name}' with accessibility='$accessibility', isReadonly='$isReadonly', fieldDefaultValue='$fieldDefaultValue'")

                m_instanceMembers[property.name] = Field(accessibility, isReadonly, fieldDefaultValue)
            }
            // TODO add support for property.returnType.isSubtypeOf(TypeContainer::class.starProjectedType) for even easier access to object instances
        }
    }
}