package stdlib.libstruct

import stdlib.values.Value

abstract class ClassMember (
    open val accessibility: Accessibility = Accessibility.Private,
    open var value: Value? = null
)