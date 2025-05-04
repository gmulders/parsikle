# Parsikle

**A lightweight but powerful parser combinator library for Kotlin 🚀**

Parsikle is a Kotlin Multiplatform parser combinator library designed to make it easy (and even fun) to construct
complex parsers from simple building blocks. Its goal is to be expressive, concise, and usable across platforms.

---

## 📦 Getting Started

Parsikle is available on Maven Central.

### Add to your Gradle project:

```kotlin
implementation("io.github.gmulders.parsikle:core:0.0.3")
```
If you’re using Kotlin Multiplatform (KMP), add it under the appropriate sourceSet:

```kotlin
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation("io.github.gmulders.parsikle:core:0.0.3")
            }
        }
    }
}
```

## ✨ Example
Here’s an example:
```kotlin
import io.github.gmulders.parsikle.core.*

// A number parser: one non-zero digit then any number of digits → Int,
// Note that this is a bit contrived, normally you'd use a tokenParser, that
// uses a Regex.
val number: Parsikle<Int> =
    digit
        .filter("Number must not start with '0'") { it != '0' }
        .then(digit.many())
        .map { (first, rest) ->
            (listOf(first) + rest)
                .joinToString("")
                .toInt()
        }

// A '+' parser
val plus: Parsikle<Char> = parse('+')

// An addition expression parser: Int '+' Int
val addExpr: Parsikle<Pair<Int, Int>> =
    number thenIgnore plus then number

// Helper to run and print a parser result
fun <T> runParser(p: Parsikle<T>, input: String) {
    val result = p(ParserState(input))
    when (result) {
        is Success -> println("Success → ${result.value}")
        is Failure ->  println("Error → ${result.error.message}")
    }
}

fun main() {
    runParser(number, "42")           // Success → 42
    runParser(addExpr, "3+7")         // Success → (3, 7)
    runParser(addExpr, "03+7")        // Error → Number must not start with '0'
}

```

---

## 🧪 Testing Support
There’s also a dedicated test module with helpers and matchers:
```kotlin
implementation("io.github.gmulders.parsikle:parsikle-test:0.0.3")
```

---

## 📚 Modules
- `parsikle-core`: The core parsing primitives and combinators
- `parsikle-test`: Utilities for testing parsers (e.g. assertions, golden tests)

---

## 📖 Documentation
> Coming soon! (KDoc, usage guide, parser recipes, etc.)

## 🔗 License
[MIT](https://opensource.org/license/MIT) Geert Mulders