package parser.ast.errors

class ReferenceError constructor(override val message: String?) : Exception(message)
{
    companion object {
        fun isNotDefined(name: String) : ReferenceError {
            return ReferenceError("'$name' is not defined")
        }
    }
}