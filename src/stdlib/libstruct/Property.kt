package stdlib.libstruct

import stdlib.libstruct.functions.Callable
import stdlib.values.Value

class Property(
        override val accessibility: Accessibility = Accessibility.Private,
        val static: Boolean = false,
        val getter: Callable? = null,
        val setter: Callable? = null,
        override var value: Value? = null
) : ClassMember(accessibility, value)