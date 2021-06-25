package stdlib.libstruct

import parser.tokenizer.TokenType
import stdlib.libstruct.functions.Callable
import stdlib.libstruct.functions.FunctionWithOverloads


class OperatorOverloadTable {
    val unary = hashMapOf<TokenType, FunctionWithOverloads>()
    val binary = hashMapOf<TokenType, FunctionWithOverloads>()
}