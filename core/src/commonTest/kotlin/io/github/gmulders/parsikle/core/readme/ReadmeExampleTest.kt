package io.github.gmulders.parsikle.core.readme

import io.github.gmulders.parsikle.core.*
import kotlin.test.Test

class ReadmeExampleTest {

    @Test
    fun `example from readme`() {
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

        runParser(number, "42")           // Success → 42
        runParser(addExpr, "3+7")         // Success → (3, 7)
        runParser(addExpr, "03+7")        // Error → Number must not start with '0'
    }
}
