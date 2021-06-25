package stdlib.libstruct

import stdlib.values.Value

class Field(
        override val accessibility: Accessibility = Accessibility.Private,
        val readonly: Boolean = false, // val -> true, var -> false
        override var value: Value? = null
) : ClassMember(accessibility, value)