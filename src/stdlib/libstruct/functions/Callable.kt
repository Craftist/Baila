package stdlib.libstruct.functions

import stdlib.values.Value

abstract class Callable {
    /**
     * Call the callable object.
     * @param args Callable arguments array. Does not typecheck, since types are checked by the corresponding function.
     * @return Return value. If void, the return value is null.
     */
    abstract fun call(args: Array<Value>): Value?

    companion object {
        val emptyFunc: Callable = object : Callable() {
            override fun call(args: Array<Value>): Value? {
                println("Calling emptyFunc")
                return null
            }
        }
    }
}