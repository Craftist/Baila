package stdlib.libstruct

abstract class Class(val thisType: String, val inheritingType: String? = "Object") : TypeContainer() {
    override fun getType() = BailaType(thisType)

    override fun equals(other: Any?): Boolean {
        if (other is Class) {
            return thisType == other.thisType
        }

        return false
    }

    override fun hashCode(): Int {
        var result = thisType.hashCode()
        result = 31 * result + (inheritingType?.hashCode() ?: 0)
        result = 31 * result + m_constructor.hashCode()
        result = 31 * result + m_instanceMembers.hashCode()
        result = 31 * result + m_staticMembers.hashCode()
        result = 31 * result + m_operators.hashCode()
        return result
    }
}