package parser.ast.exceptions

import parser.ast.expressions.Expression

class ReturnException(val expr: Expression? = null) : Exception()