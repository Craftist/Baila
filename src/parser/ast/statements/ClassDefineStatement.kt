package parser.ast.statements

import parser.ast.Parser
import stdlib.NameTable
import stdlib.libstruct.BailaType
import stdlib.libstruct.Class
import stdlib.libstruct.Field
import stdlib.libstruct.functions.FunctionOverload
import stdlib.values.classes.Type
import stdlib.values.objects.ObjectValue

class ClassDefineStatement(val className: String, val inheritingTypes: ArrayList<BailaType>, val members: ArrayList<Parser.ClassMemberDeclaration>) : Statement {
    override fun execute() {
        var inheritingClass: Class? = null

        for (inheritingType in inheritingTypes) {
            val type = NameTable.getClass(inheritingType.className) as? Type
                ?: throw Exception("Type $inheritingType is not a Type! Error ID: vus08fjs0dvfjsdzhcf3789ghdc87ergf8ew7rfweuf")

            if (type.correspondingTypeContainer is Class) {
                if (inheritingClass != null) {
                    throw Exception("Multiple inheritance is not allowed: " +
                            "${inheritingClass.thisType} is already a class $className is inheriting from, " +
                            "$inheritingType cannot be an inheriting class. Error ID: fwyhef7ywe89fyhwe9fhjwe9fhef7ef87fhcsijh97wef")
                }

                inheritingClass = type.correspondingTypeContainer
            } // else if (type.correspondingTypeContainer is Interface) ... multiple interfaces are allowed
        }

        class ___DefinedClass : Class(className, inheritingClass?.inheritingType) {
            init {
                m_constructor.add(FunctionOverload.constructor(this, arrayListOf()) {})

                /*val instanceFields = members.filterIsInstance<Parser.ClassFieldDeclaration>().filter { !it.isStatic }
                val staticFields = members.filterIsInstance<Parser.ClassFieldDeclaration>().filter { it.isStatic }
                val methods = members.filterIsInstance<Parser.ClassMethodDeclaration>()
                val properties = members.filterIsInstance<Parser.ClassPropertyDeclaration>()

                for (field in instanceFields) {
                    m_instanceMembers[field.name] = Field(
                        field.accessibility,
                        false,
                        field.defaultValue?.eval()
                    )
                }*/
            }
        }

        NameTable.scope.addConstant(className, ObjectValue(NameTable.createHeapObject(
            Type(___DefinedClass())
        )))
    }

    override fun toString(): String {
        return "ClassDefineStatement(className=$className, inheritingTypes=${inheritingTypes.joinToString()}, ${members.size} members)"
    }
}