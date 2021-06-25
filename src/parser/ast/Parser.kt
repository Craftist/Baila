package parser.ast

import parser.ast.errors.SyntaxError
import parser.ast.expressions.*
import parser.ast.statements.*
import parser.tokenizer.Cursor
import parser.tokenizer.Token
import parser.tokenizer.TokenGroup
import parser.tokenizer.TokenType
import stdlib.libstruct.Accessibility
import stdlib.libstruct.BailaType
import stdlib.libstruct.functions.*
import stdlib.values.primitives.BooleanValue
import stdlib.values.primitives.ListValue
import stdlib.values.primitives.NumberValue
import stdlib.values.primitives.StringValue
import java.util.*
import kotlin.collections.ArrayList

class Parser(private val tokens: List<Token>) {
    private val length = tokens.size

    private val rollbacks = Stack<Int>()

    private var pos = 0

    /*
    TODO Object Oriented Programming (basics)
    */

    // --------------------------------------------------

    fun parse(): Statements {
        val result = Statements()
        while (!match(TokenType.EOF)) {
            result.addStatement(statement())
        }
        return result
    }

    // --------------------------------------------------

    // Number, ?Boolean, <String>List, ?<Number, ?<Number>List>Dictionary
    // The syntax is the reverse of, say, C#, in order to avoid syntax ambiguity
    private fun type(): BailaType {
        // Nullable check first
        val isNullable = match(TokenType.Question)

        // Generics second
        val genericsList = arrayListOf<BailaType>()
        if (match(TokenType.Lt)) {
            while (!match(TokenType.Gt)) {
                val genericType = type()
                genericsList.add(genericType)

                if (match(TokenType.Gt)) break

                consume(TokenType.Comma)
            }
        }

        // Type name (identifier) last
        val ret: BailaType = BailaType(consume(TokenType.Identifier).getValue(), isNullable)
        genericsList.forEach { ret.addGeneric(it) }

        return ret
    }

    private fun statement(): Statement {
        var stmt: Statement? = null

        if (match(TokenType.If)) {
            stmt = ifElse()
        } else if (match(TokenType.For)) {
            stmt = forStatement()
        } else if (match(TokenType.While)) {
            stmt = whileStatement()
        } else if (match(TokenType.Do)) {
            stmt = doWhileStatement()
        }
        //if (get(0).getType() == TokenType.Identifier && get(0).getValue() == "print") {
        //    consume(TokenType.Identifier)
        //    val v = expression()
        //    return object: Statement {
        //        override fun execute() {
        //            println(v.eval().asString())
        //        }
        //    }
        //}
        else if (match(TokenType.Var)) {
            val name = consume(TokenType.Identifier).getValue()
            var type: BailaType? = null
            var value: Expression? = null
            if (match(TokenType.Colon)) {
                // Type specification, if omitted, the type will be inferred from the value
                type = type()
            }
            if (match(TokenType.Eq)) {
                value = expression()
            }
            stmt = VariableDefineStatement(name, type, value)
        } else if (match(TokenType.Const)) {
            val name = consume(TokenType.Identifier).getValue()
            consume(TokenType.Eq)
            val value = expression()
            stmt = ConstantDefineStatement(name, value)
        } else if (match(TokenType.Function)) {
            stmt = function()
        } else if (match(TokenType.Return)) {
            stmt = if (lookMatch(0, TokenType.RightCurly)) {
                // Empty return statement
                ReturnStatement()
            } else {
                ReturnStatement(expression())
            }
        }

        // class Name { ... }
        // class Name : ParentClass { ... }
        // class Name : Interface1, Interface2, ..., InterfaceN { ... }
        // class Name : ParentClass, Interface1, Interface2, ..., InterfaceN { ... }
        else if (match(TokenType.Class)) {
            val className = consume(TokenType.Identifier).getValue();

            val inheriting = arrayListOf<BailaType>()
            if (match(TokenType.Colon)) {
                while (true) {
                    inheriting.add(type())

                    if (lookMatch(0, TokenType.LeftCurly)) break;

                    consume(TokenType.Comma)
                }
            }

            consume(TokenType.LeftCurly)

            val members = arrayListOf<ClassMemberDeclaration>()

            // Add members
            while (true) {
                classMember(className)?.let { members.add(it) }

                if (lookMatch(0, TokenType.RightCurly)) {
                    break
                }
            }

            consume(TokenType.RightCurly)

            Debug.log("Create class $className that inherits from ${if (inheriting.size > 0) inheriting.joinToString { it.className } else "nothing"}")
            Debug.log("  with ${members.filterIsInstance<ClassFieldDeclaration>().size} fields: ${members.filterIsInstance<ClassFieldDeclaration>().joinToString { "${it.name}${if (it.isReadonly) "(readonly)" else ""}" }}")
            Debug.log("  with ${members.filterIsInstance<ClassMethodDeclaration>().size} methods: ${members.filterIsInstance<ClassMethodDeclaration>().joinToString { it.name }}")
            Debug.log("  with ${members.filterIsInstance<ClassPropertyDeclaration>().size} properties: ${members.filterIsInstance<ClassPropertyDeclaration>().joinToString {
                val gs = arrayListOf<String>()
                if (it.getter != null) { gs.add("get") }
                if (it.setter != null) { gs.add("set") }
                "${it.name}(${gs.joinToString()})"
            }}")
            stmt = ClassDefineStatement(className, inheriting, members)
        } else {
            stmt = ExpressionStatement(expression())
        }

        if (!lookMatch(0, TokenType.EOF) && !lookMatch(0, TokenType.RightCurly) && !lookMatch(0, TokenType.Semicolon)) {
            unexpectedToken(get())
        }

        if ((lookMatch(0, TokenType.EOF) || lookMatch(0, TokenType.Semicolon)) && pos < length)
            ++pos // skip EOF or ;

        return stmt
    }

    private fun function(): FunctionDefineStatement {
        val name = consume(TokenType.Identifier).getValue()
        val parameters = ArrayList<Parameter>()

        var returnType: BailaType? = null

        if (match(TokenType.LeftParen)) {
            // TODO varargs
            while (!match(TokenType.RightParen)) {
                val parameterName = consume(TokenType.Identifier).getValue()
                consume(TokenType.Colon)
                val typeName = consume(TokenType.Identifier).getValue()
                var defaultValue: Expression? = null
                if (match(TokenType.Eq)) {
                    defaultValue = expression()
                }

                parameters.add(Parameter(parameterName, BailaType(typeName), defaultValue))

                if (match(TokenType.RightParen)) break

                consume(TokenType.Comma)
            }
        }

        // optional return type
        var shouldTraverseBody = false
        if (match(TokenType.Colon)) {
            // todo redo to catch fully optional expression
            returnType = BailaType(consume(TokenType.Identifier).getValue())
        } else {
            // Return type is not specified, try to parse the return type from the body
            // If there are no return statements then the function is void.
            // ExprStmt are supported only with explicitly defined return type in order to avoid
            //   func f { g() } of picking up the 'g' return type (if it's non-void).
            //   In that case func f : Number { g() } is the correct syntax if 'g' returns a Number.

            shouldTraverseBody = true

            // todo traverse function statements and retrieve all return statements, if any
        }

        val body = functionStatementBlock()

        if (returnType == null) {
            returnType = when (body) {
                is ReturnStatement -> {
                    if (body.expr == null) {
                        throw Exception("Cannot infer function return type: not enough data given")
                    }
                    BailaType("String") // TODO fix that shit (get type from STATEMENT)
                }
                is BlockStatement -> BailaType("String") // TODO fix that shit (get type from STATEMENT)
                else -> throw Exception("Unsupported type of statement: ${body.javaClass.name}")
            }
        }

        return FunctionDefineStatement(name, parameters, body, returnType)
    }

    open class ClassMemberDeclaration

    class ClassFieldDeclaration(
        val accessibility: Accessibility,
        val isStatic: Boolean,
        val isReadonly: Boolean,
        val name: String,
        val type: BailaType?,
        val defaultValue: Expression?
    ) : ClassMemberDeclaration()

    class ClassPropertyDeclaration(
        val accessibility: Accessibility,
        val isStatic: Boolean,
        val getter: Callable? = null,
        val setter: Callable? = null,
        val name: String,
        val type: BailaType?,
        val defaultValue: Expression?
    ) : ClassMemberDeclaration()

    class ClassMethodDeclaration(
        val accessibility: Accessibility,
        val isStatic: Boolean,
        val name: String,
        val parameters: ArrayList<Parameter>,
        val returnType: BailaType?,
        val body: Statement
    ) : ClassMemberDeclaration()

    private fun classMember(className: String): ClassMemberDeclaration? {
        var member: ClassMemberDeclaration? = null

        var accessibility: Accessibility? = null
        var isStatic: Boolean = false

        while (lookMatch(0, TokenType.Public) || lookMatch(0, TokenType.Private)
            || lookMatch(0, TokenType.Protected) || lookMatch(0, TokenType.Static)
        ) {
            if (match(TokenType.Static)) {
                if (isStatic) {
                    // Duplicate static modifier
                    throw SyntaxError.duplicateStaticModifier(className)
                }

                isStatic = true
            } else {
                when {
                    accessibility != null -> {
                        // Duplicate accessibility modifier
                        throw SyntaxError.duplicateAccessibilityModifier(className)
                    }
                    match(TokenType.Public) -> {
                        accessibility = Accessibility.Public
                    }
                    match(TokenType.Private) -> {
                        accessibility = Accessibility.Private
                    }
                    match(TokenType.Protected) -> {
                        accessibility = Accessibility.Protected
                    }
                }
            }
        }

        if (accessibility == null) accessibility = Accessibility.Private // default accessibility is private

        if (match(TokenType.Var)) {
            // Member non-readonly field
            val fieldName = consume(TokenType.Identifier).getValue()
            val isReadonly = false
            var type: BailaType? = null
            var value: Expression? = null
            if (match(TokenType.Colon)) {
                // Type specification, if omitted, the type will be inferred from the value
                type = type()
            }
            if (match(TokenType.Eq)) {
                value = expression()
            }

            member = ClassFieldDeclaration(
                accessibility ?: Accessibility.Private, // default class member accessbility is private
                isStatic,
                isReadonly,
                fieldName,
                type,
                value
            )

        } else if (match(TokenType.Const)) {
            // Member readonly field
            val fieldName = consume(TokenType.Identifier).getValue()
            val isReadonly = true
            var type: BailaType? = null
            var value: Expression? = null
            if (match(TokenType.Colon)) {
                // Type specification, if omitted, the type will be inferred from the value
                type = type()
            }
            if (match(TokenType.Eq)) {
                value = expression()
            }

            member = ClassFieldDeclaration(
                accessibility ?: Accessibility.Private, // default class member accessbility is private
                isStatic,
                isReadonly,
                fieldName,
                type,
                value
            )

        } else if (match(TokenType.Property)) {
            // Member property

            val propName = consume(TokenType.Identifier).getValue()
            val isReadonly = true
            var propType: BailaType? = null
            var value: Expression? = null
            if (match(TokenType.Colon)) {
                // Type specification, if omitted, the type will be inferred from the value
                propType = type()
            }
            if (match(TokenType.Eq)) {
                value = expression()
            }

            consume(TokenType.LeftCurly)

            var getter: Callable? = null
            var setter: Callable? = null // maybe actually make overloads so that you can do stuff like this:
            /*
            class MyClass {
              public property x: String {
                get # default implementation: get = field (return backing field)
                set              { println("Setting string"); field = "s" + value            } # without parentheses: implied type of property: set(value: String) => ...
                set(value: Int)  { println("Setting int");    field = "i" + value.toString() }
                set(value: Bool) { println("Setting bool");   field = "b" + (value ? 1 : 0)  }
              }
            }

            const mc = MyClass()
            mc.x = "Hello" # Prints "Setting string"
            println(mc.x) # Prints "sHello"
            mc.x = 123 # Prints "Setting int"
            println(mc.x) # Prints "i123"
            mc.x = true # Prints "Setting bool"
            println(mc.x) # Prints "b123"
            */

            while (true) {
                if (isSoftKeyword(0, "get")) {
                    if (getter != null) throw SyntaxError.duplicateGetter(className, propName)

                    consume(TokenType.Identifier) // skip 'get' soft keyword
                    getter = StatementCallable(functionStatementBlock())
                } else if (isSoftKeyword(0, "set")) {
                    if (setter != null) throw SyntaxError.duplicateSetter(className, propName)

                    consume(TokenType.Identifier) // skip 'set' soft keyword
                    setter = StatementCallable(functionStatementBlock())
                }

                if (lookMatch(0, TokenType.RightCurly)) { break }
                if (!lookMatch(0, TokenType.EOF) && !lookMatch(0, TokenType.Semicolon)) { unexpectedToken(get()) }
                if (pos < length) ++pos // skip EOF or ;
            }

            consume(TokenType.RightCurly)

            var propDefaultValue: Expression? = null
            if (match(TokenType.Eq)) {
                propDefaultValue = expression()
            }

            member = ClassPropertyDeclaration(
                accessibility ?: Accessibility.Private, // default class member accessbility is private
                isStatic,
                getter,
                setter,
                propName,
                propType,
                propDefaultValue
            )

        } else if (match(TokenType.Function)) {
            // Member function (can be overriden)
            val function = function()
            return ClassMethodDeclaration(
                accessibility,
                isStatic,
                function.name,
                function.parameters,
                function.returnType,
                function.body
            )
        }

        // Require either a new line, ';' or '}' after a member
        if (!lookMatch(0, TokenType.EOL) && !lookMatch(0, TokenType.Semicolon) && !lookMatch(0, TokenType.RightCurly)) {
            throw SyntaxError.unexpectedToken(get(), arrayOf(TokenType.EOL, TokenType.Semicolon, TokenType.RightCurly))
        }

        if (lookMatch(0, TokenType.EOL) || lookMatch(0, TokenType.Semicolon)) {
            pos++ // skip EOL or ; respectively
            // don't skip } because it will be consumed after the last member
        }

        return member
        //throw Exception("Unexpected token in class member declaration: ${get(0).getType()}")
    }

    private fun statementBlock(): Statement {
        val block = BlockStatement()
        consume(TokenType.LeftCurly)
        while (!match(TokenType.RightCurly)) {
            block.addStatement(statement())
        }
        return block
    }

    private fun functionStatementBlock(): Statement {
        if (match(TokenType.Eq)) {
            return ReturnStatement(expression())
        }

        if (match(TokenType.DoubleArrow)) {
            return statementOrBlock()
        }

        val block = BlockStatement()
        consume(TokenType.LeftCurly)
        while (true) {
            block.addStatement(statement())

            if (match(TokenType.RightCurly)) break;
            if (match(TokenType.EOF)) break;
        }

        return block
    }

    private fun statementOrBlock(): Statement {
        if (lookMatch(0, TokenType.LeftCurly)) return statementBlock()
        return statement()
    }

    private fun ifElse(): Statement {
        val condition = expression()
        val trueStmt = statementOrBlock()
        var falseStmt: Statement? = null
        if (match(TokenType.Else)) {
            falseStmt = statementOrBlock()
        }
        return IfElseStatement(condition, trueStmt, falseStmt)
    }

    private fun forStatement(): Statement {
        val optionalLeftParen = match(TokenType.LeftParen)

        val varName = consume(TokenType.Identifier).getValue()
        consume(TokenType.Eq)
        val initial = expression()

        if (!(get().getType() == TokenType.Identifier && get().getValue() == "to")) {
            throw Exception("Syntax Error: 'to' expected in 'for' loop. For C-like style of 'for' loop, please use 'while' instead.")
        }
        match(TokenType.Identifier)

        val final = expression()

        val step: Expression = if (get().getType() == TokenType.Identifier && get().getValue() == "step") {
            match(TokenType.Identifier)
            expression()
        } else {
            ValueExpression(NumberValue(1.0))
        }

        if (optionalLeftParen) match(TokenType.RightParen)

        val body = statementOrBlock()

        return ForStatement(varName, initial, final, step, body)
    }

    private fun whileStatement(): Statement {
        val condition = expression()
        val body = statementOrBlock()

        return WhileStatement(condition, body)
    }

    private fun doWhileStatement(): Statement {
        val body = statementOrBlock()
        consume(TokenType.While)
        val condition = expression()

        return DoWhileStatement(condition, body)
    }

    // --------------------------------------------------

    private fun expression(): Expression {
        return assignment()
    }

    // the lowest precedence
    // TODO make possible to `EXPR =` not only `IDENT =`
    private fun assignment(): Expression {
        //if (lookMatch(0, TokenType.Identifier) && lookMatch(1, TokenType.Eq)) {
        if (lookMatch(0, TokenType.Identifier) && lookMatch(1, TokenGroup.Assignment)) {
            val name = consume(TokenType.Identifier).getValue()
            val assignmentToken = get(); match(TokenGroup.Assignment)
            val expr = assignment()

            val type = assignmentToken.getType()
            if (type == TokenType.Eq) {
                return AssignmentExpression(name, expr)
            }

            return AssignmentExpression(name, expr, type)
        }
        return bitwiseOr()
    }

    // TODO binaryOr
    // TODO binaryAnd

    private fun bitwiseOr(): Expression {
        var result = bitwiseXor()
        while (true) {
            if (match(TokenType.Bar)) {
                result = BinaryExpression(TokenType.Bar, result, bitwiseXor())
                continue
            }
            break
        }
        return result
    }

    private fun bitwiseXor(): Expression {
        var result = bitwiseAnd()
        while (true) {
            if (match(TokenType.Caret)) {
                result = BinaryExpression(TokenType.Caret, result, bitwiseAnd())
                continue
            }
            break
        }
        return result
    }

    private fun bitwiseAnd(): Expression {
        var result = equality()
        while (true) {
            if (match(TokenType.Amp)) {
                result = BinaryExpression(TokenType.Amp, result, equality())
                continue
            }
            break
        }
        return result
    }

    private fun equality(): Expression {
        var result = numberRelation()
        while (true) {
            if (match(TokenType.EqEq)) {
                result = BinaryExpression(TokenType.EqEq, result, numberRelation())
                continue
            }
            if (match(TokenType.ExclEq)) {
                result = BinaryExpression(TokenType.ExclEq, result, numberRelation())
                continue
            }
            break
        }
        return result
    }

    private fun numberRelation(): Expression {
        var result = addition()
        while (true) {
            if (match(TokenType.Lt)) {
                result = BinaryExpression(TokenType.Lt, result, addition())
                continue
            }
            if (match(TokenType.LtEq)) {
                result = BinaryExpression(TokenType.LtEq, result, addition())
                continue
            }
            if (match(TokenType.Gt)) {
                result = BinaryExpression(TokenType.Gt, result, addition())
                continue
            }
            if (match(TokenType.Gt)) {
                result = BinaryExpression(TokenType.GtEq, result, addition())
                continue
            }
            break
        }
        return result
    }

    private fun addition(): Expression {
        var result = multiplication()
        while (true) {
            if (match(TokenType.Plus)) {
                result = BinaryExpression(TokenType.Plus, result, multiplication())
                continue
            }
            if (match(TokenType.Minus)) {
                result = BinaryExpression(TokenType.Minus, result, multiplication())
                continue
            }
            break
        }
        return result
    }

    private fun multiplication(): Expression {
        var result = power()
        while (true) {
            if (match(TokenType.Slash)) {
                result = BinaryExpression(TokenType.Slash, result, power())
                continue
            } else if (match(TokenType.SlashSlash)) {
                result = BinaryExpression(TokenType.SlashSlash, result, power())
                continue
            } else if (match(TokenType.Star)) {
                result = BinaryExpression(TokenType.Star, result, power())
                continue
            }
            break
        }
        return result
    }

    private fun power(): Expression {
        var result = unary()
        while (true) {
            if (match(TokenType.StarStar)) {
                result = BinaryExpression(TokenType.StarStar, result, unary())
                continue
            }
            break
        }
        return result
    }

    private fun unary(): Expression {
        if (match(TokenType.Tilde)) {
            return UnaryExpression(UnaryExpression.Operator.BitwiseNot, unary())
        }
        if (match(TokenType.Excl)) {
            return UnaryExpression(UnaryExpression.Operator.LogicalNot, unary())
        }
        if (match(TokenType.Plus)) {
            return UnaryExpression(UnaryExpression.Operator.Plus, unary())
        }
        if (match(TokenType.Minus)) {
            return UnaryExpression(UnaryExpression.Operator.Minus, unary())
        }
        return primary()
    }

    private fun traceTokenMap(): List<String> {
        return tokens.mapIndexed { index, token -> if (pos == index) ">>>$token<<<" else token.toString() }
    }

    private var tokenMap: List<String> = traceTokenMap()

    private fun primary(): Expression {
        val current = get()

        var expression: Expression? = null

        // (expression)
        if (match(TokenType.LeftParen)) {
            val expr = expression()
            consume(TokenType.RightParen)
            expression = expr
        }

        // [ lists, ... ]
        else if (match(TokenType.LeftBracket)) {
            val exprList = ArrayList<Expression>()

            while (!match(TokenType.RightBracket)) {
                exprList.add(expression())
                match(TokenType.Comma) // TODO require comma
            }

            val lv = ListValue()
            lv.list.addAll(exprList.map { it.eval() })
            expression = ValueExpression(lv)
        }

        // Numbers
        else if (match(TokenType.NumberLiteral)) {
            val currentNum = current.getValue()
            val suffix = currentNum[currentNum.length - 1]
            val num = currentNum.substring(0 until currentNum.length - 1)

            expression = when (suffix) {
                'c' -> ValueExpression(StringValue(num.toInt().toChar().toString()))
                else -> ValueExpression(NumberValue(currentNum.toDouble()))
            }
        }

        // Strings
        else if (match(TokenType.StringLiteral)) {
            expression = ValueExpression(StringValue(current.getValue()))
        }

        // true and false
        else if (match(TokenType.True)) {
            expression = ValueExpression(BooleanValue(true))
        } else if (match(TokenType.False)) {
            expression = ValueExpression(BooleanValue(false))
        }

        // Variables and constants
        else if (match(TokenType.Identifier)) {
            expression = VariableExpression(current.getValue())
        }

        // If no match then throw unexpected token
        if (expression == null) {
            throw SyntaxError.unexpectedToken(current)
        }

        while (true) {

            // ultimate test:
            // obj.abc[123].def.ghi()[jkl][mno].p[q].r(s).tuv.w[x.y[z]]


            // Function call
            /* fixme make [] and () parsing AFTER the primary parse so you can index and call any expression
                 even like (1, 2, 3)[2] tuples and (x => y => x ** y)(6)(2) lambdas */
            while (lookMatch(0, TokenType.LeftParen)) {
                val args = ArrayList<Expression>()
                consume(TokenType.LeftParen)

                while (!match(TokenType.EOF) && !match(TokenType.RightParen)) {
                    args.add(expression())
                    if (match(TokenType.RightParen))
                        break
                    consume(TokenType.Comma)
                }
                expression = FunctionCallExpression(expression!!, args)
            }

            // Elements after expression:
            // EXPR [ expr ] : Indexer access
            while (match(TokenType.LeftBracket)) {
                val indexerExpr = expression()
                consume(TokenType.RightBracket) // after expression right bracket is required
                expression = IndexerAccessExpression(expression!!, indexerExpr)
            }

            // EXPR . ident : Object dot access
            while (match(TokenType.Dot)) {
                val propName = consume(TokenType.Identifier).getValue()
                expression = ObjectDotGetExpression(expression!!, propName)
            }

            if (!lookMatch(0, TokenType.LeftParen) && !lookMatch(0, TokenType.LeftBracket) && !lookMatch(0, TokenType.Dot)) break
        }

        if (lookMatch(0, TokenType.Eq)) {
            consume(TokenType.Eq)
            if (expression is ObjectDotGetExpression) {
                expression = ObjectDotSetExpression(expression.valueExpr, expression.fieldIdent, expression())
            } else {
                throw Exception("Unknown expression type to set a new value to: $expression")
            }
        }

        return expression!!

        // TODO closures (anonymous functions)
    }

    // --------------------------------------------------

    private fun saveRollback() {
        rollbacks.push(pos)
    }

    private fun rollback() {
        if (rollbacks.isEmpty()) {
            throw Exception("No saved rollbacks to rollback. Use saveRollback()")
        }

        pos = rollbacks.pop()
    }

    // --------------------------------------------------

    private fun get(rel: Int = 0): Token {
        return getAbsolute(pos + rel)
    }

    private fun getAbsolute(abs: Int): Token {
        tokenMap = traceTokenMap()

        if (abs < 0) throw Exception()
        if (abs >= length) {
            return try {
                val prev = getAbsolute(abs - 1)
                Token(Cursor(prev.getLineNo(), prev.getLineCol(), prev.getPosition(), prev.getFileName(), prev.getCodeLine()), TokenType.EOF)
            } catch (e: Exception) {
                Token(Cursor(0, 0, 0, "", ""), TokenType.EOF)
            }
        }

        return tokens[abs]
    }

    private fun match(type: TokenType): Boolean {
        if (get().getType() != type) {
            return false
        }
        ++pos
        tokenMap = traceTokenMap()
        return true
    }

    private fun match(group: TokenGroup): Boolean {
        if (!get().getType().satisfiesGroup(group)) {
            return false
        }
        ++pos
        tokenMap = traceTokenMap()
        return true
    }

    private fun consume(type: TokenType): Token {
        val current = get()
        if (current.getType() != type) {
            throw SyntaxError.unexpectedToken(unexpected = current, expectedType = type)
        }
        ++pos
        tokenMap = traceTokenMap()
        return current
    }

    private fun consume(type: TokenType, value: String): Token {
        val current = get()
        if (current.getType() != type && current.getValue() != value) {
            throw SyntaxError.unexpectedToken(unexpected = current, expectedType = type)
        }
        ++pos
        return current
    }

    fun unexpectedToken(type: Token) {
        throw SyntaxError.unexpectedToken(unexpected = type)
    }

    // TODO make this work with groups and show all the expected tokens
    /*private fun consume(type: TokenGroup, value: String? = null) : Token {
        val current = get()
        if (!current.getType().satisfiesGroup(type) && value != null && current.getValue() == value) {
            throw Exception("Unexpected ${current.getType()}, $type expected")
        }
        ++pos
        return current
    }*/

    private fun lookMatch(pos: Int, type: TokenType): Boolean {
        tokenMap = traceTokenMap()
        return get(pos).getType() === type
    }

    private fun lookMatch(pos: Int, group: TokenGroup): Boolean {
        tokenMap = traceTokenMap()
        return get(pos).getType().satisfiesGroup(group)
    }

    private fun isSoftKeyword(pos: Int, value: String) : Boolean {
        return lookMatch(0, TokenType.Identifier) && get(0).getValue() == value
    }
}