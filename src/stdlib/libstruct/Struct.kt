package stdlib.libstruct

class Struct(private val type: String) : TypeContainer() {
    override fun getType() = BailaType(type)
}