package stdlib.values.objects

// class TypeValue(val typeContainer: TypeContainer) : Value() {
//     override fun asNumber() = throw TypeError.cannotConvert(typeContainer.getType(), Type("Number"))
//     override fun asString() = typeContainer.getType().toString()
//     override fun asBoolean() = throw TypeError.cannotConvert(typeContainer.getType(), Type("Boolean"))
//     override fun asFunction() = throw TypeError.cannotConvert(typeContainer.getType(), Type("Function"))
//
//     override fun toString() = asString()
//
//     override fun getType() = Type("Type")
//
//     override fun getDefaultValue() = TypeValue(Class(Type("Type")))
// }