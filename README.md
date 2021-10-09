# About

`Baila` is my dream programming language.

- Baila is a scripting language, yet it is statically typed (I believe in type safety)
- Baila treats everything as a value: variables, expressions, functions, heck, even types are values
  (you can pass types, literal types (like `Int` or `<Int>List`) into functions and create them.
  Type parameters (generics) are not needed, even though Baila supports them as well)
- Syntax is heavily inspired from Kotlin, my probably most favorite language, but also some
  features are taken from JavaScript, some from C#

Some examples of Baila:
```js
function greet(who: String) {
    println("Hello, $who!")
}

greet("Baila programmer") # Outputs "Hello, Baila programmer!"
```
