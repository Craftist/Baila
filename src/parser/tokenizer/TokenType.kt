package parser.tokenizer

enum class TokenGroup {
    General,
    Assignment
}

enum class TokenType (val type: String, private val group: TokenGroup, val subtractFromPos: Int = 0) {
    EOF("EOF", TokenGroup.General),
    EOL("EOL", TokenGroup.General),

    Identifier("IDENTIFIER", TokenGroup.General),
    StringLiteral("STRING", TokenGroup.General, 1),
    NumberLiteral("NUMBER", TokenGroup.General),
    RegexLiteral("REGEX", TokenGroup.General, 1),

    // operators
    Plus("+", TokenGroup.General),
    PlusPlus("++", TokenGroup.General),
    Minus("-", TokenGroup.General),
    MinusMinus("--", TokenGroup.General),
    Star("*", TokenGroup.General),
    StarStar("**", TokenGroup.General),
    Slash("/", TokenGroup.General),
    SlashSlash("//", TokenGroup.General),
    Percent("%", TokenGroup.General),

    Eq("=", TokenGroup.Assignment),
    EqEq("==", TokenGroup.General),
    EqEqEq("===", TokenGroup.General),
    ExclEq("!=", TokenGroup.General),
    ExclEqEq("!==", TokenGroup.General),
    PlusEq("+=", TokenGroup.Assignment),
    MinusEq("-=", TokenGroup.Assignment),
    StarEq("*=", TokenGroup.Assignment),
    StarStarEq("**=", TokenGroup.Assignment),
    SlashEq("/=", TokenGroup.Assignment),
    SlashSlashEq("//=", TokenGroup.Assignment),
    PercentEq("%=", TokenGroup.Assignment),
    BarEq("|=", TokenGroup.Assignment),
    BarBarEq("||=", TokenGroup.Assignment),
    AmpEq("&=", TokenGroup.Assignment),
    AmpAmpEq("&&=", TokenGroup.Assignment),
    CaretEq("^=", TokenGroup.Assignment),
    CaretCaretEq("^=", TokenGroup.Assignment),

    Bar("|", TokenGroup.General),
    BarBar("||", TokenGroup.General),
    Amp("&", TokenGroup.General),
    AmpAmp("&&", TokenGroup.General),
    Pipe("|>", TokenGroup.General),
    Caret("^", TokenGroup.General),
    CaretCaret("^^", TokenGroup.General),
    Tilde("~", TokenGroup.General),

    Dot(".", TokenGroup.General),
    DotDot("", TokenGroup.General),
    NullDot("?.", TokenGroup.General),
    Comma(",", TokenGroup.General),
    Elvis("??", TokenGroup.General),
    ElvisEq("??=", TokenGroup.General),
    Excl("!", TokenGroup.General),

    Lt("<", TokenGroup.General),
    LtEq("<=", TokenGroup.General),
    LtLt("<<", TokenGroup.General),
    LtLtEq("<<=", TokenGroup.General),
    Gt(">", TokenGroup.General),
    GtEq(">=", TokenGroup.General),
    GtGt(">>", TokenGroup.General),
    GtGtEq(">>=", TokenGroup.General),
    GtGtGt(">>>", TokenGroup.General),
    GtGtGtEq(">>>=", TokenGroup.General),

    DoubleArrow("=>", TokenGroup.General),
    Semicolon(";", TokenGroup.General),

    SingleArrow("->", TokenGroup.General),
    Question("?", TokenGroup.General),
    Colon(":", TokenGroup.General),
    ColonColon("::", TokenGroup.General),

    LeftParen("(", TokenGroup.General),
    RightParen(")", TokenGroup.General),
    LeftBracket("[", TokenGroup.General),
    RightBracket("]", TokenGroup.General),
    LeftCurly("{", TokenGroup.General),
    RightCurly("}", TokenGroup.General),

    // predefined object literal keywords
    True("true", TokenGroup.General),
    False("false", TokenGroup.General),
    Null("null", TokenGroup.General),
    This("this", TokenGroup.General),
    Super("super", TokenGroup.General),

    Var("var", TokenGroup.General),
    Const("const", TokenGroup.General),
    Property("property", TokenGroup.General),
    Function("function", TokenGroup.General),
    Typeof("typeof", TokenGroup.General),
    From("from", TokenGroup.General),
    Import("import", TokenGroup.General),
    Export("export", TokenGroup.General),
    Ref("ref", TokenGroup.General),
    Operator("operator", TokenGroup.General),

    Constructor("constructor", TokenGroup.General),
    Deconstructor("deconstructor", TokenGroup.General),

    Public("public", TokenGroup.General),
    Private("private", TokenGroup.General),
    Protected("protected", TokenGroup.General),
    Sealed("sealed", TokenGroup.General),
    Static("static", TokenGroup.General),
    Global("global", TokenGroup.General),

    Async("async", TokenGroup.General),
    Await("await", TokenGroup.General),
    Class("class", TokenGroup.General),
    Struct("struct", TokenGroup.General),
    Interface("interface", TokenGroup.General),
    Enum("enum", TokenGroup.General),

    Override("override", TokenGroup.General),

    // control flow keywords
    If("if", TokenGroup.General),
    Else("else", TokenGroup.General),
    Switch("switch", TokenGroup.General),
    Match("match", TokenGroup.General),
    For("for", TokenGroup.General),
    Foreach("foreach", TokenGroup.General),
    Do("do", TokenGroup.General),
    While("while", TokenGroup.General),
    Try("try", TokenGroup.General),
    Catch("catch", TokenGroup.General),
    Finally("finally", TokenGroup.General),
    Break("break", TokenGroup.General),
    Continue("continue", TokenGroup.General),
    Return("return", TokenGroup.General),
    Throw("throw", TokenGroup.General),
    Yield("yield", TokenGroup.General),

    // operator keywords
    In("in", TokenGroup.General),
    NotIn("!in", TokenGroup.General),
    Is("is", TokenGroup.General),
    IsNot("!is", TokenGroup.General),
    As("as", TokenGroup.General),
    NullableAs("?as", TokenGroup.General);

    fun satisfiesGroup(type: TokenGroup) = group == type
}