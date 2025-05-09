package io.github.gmulders.parsikle.core

/**
 * Parser for zero-or-more whitespace characters, returning them as a String.
 *
 * Matches any `Char` satisfying `Char.isWhitespace()`, repeats with `.many()`,
 * and concatenates the matched chars into a single `String`.
 *
 * Example:
 * ```kotlin
 * whiteSpace(ParserState("  \t\nabc"))
 * // Success("  \t\n", state.index == 4)
 * ```
 */
val whiteSpace: Parsikle<String> =
    parse("Expected whitespace") { char -> char.isWhitespace() }
        .many()
        .map { list -> list.joinToString("") }

/**
 * Parser for a single-digit character.
 *
 * Matches any `Char` satisfying `Char.isDigit()`.
 *
 * Example:
 * ```kotlin
 * digit(ParserState("5a"))
 * // Success('5', state.index == 1)
 * ```
 */
val digit: Parsikle<Char> = parse("Expected digit") { char -> char.isDigit() }

/**
 * Parser for an integer that must not start with '0'.
 *
 * - Parses a first digit via `digit.filter(...)` rejecting `'0'`.
 * - Then parses zero-or-more further digits with `digit.many()`.
 * - Concatenates all digits into a `String` and converts to `Int`.
 *
 * Fails with `"Number must not start with a '0'"` if the first digit is `'0'`.
 *
 * Example:
 * ```kotlin
 * number(ParserState("42xyz"))
 * // Success(42, state.index == 2)
 * ```
 */
val number: Parsikle<Int> = digit
    .filter("Number must not start with a '0'") { it != '0' }
    .then(digit.many())
    .map { (char, list) ->
        (listOf(char) + list)
            .joinToString("")
            .toInt()
    }

/**
 * Parser that succeeds only if at end-of-input.
 *
 * Checks `state.isEof()`:
 * - On success, returns `Success(Unit, state)` without consuming any input.
 * - On failure, returns `Failure(SimpleError("Could not match end"), state)` without consuming input.
 *
 * Example:
 * ```kotlin
 * // Parser for a literal 'a' that must also be end of input
 * val aThenEnd: Parsikle<Char> = parse('a').thenIgnore(end)
 *
 * // Success: matches 'a' and then end
 * val result1 = aThenEnd(ParserState("a"))
 * // Success('a'), state.index == 1
 *
 * // Failure: matches 'a' but extra input remains
 * val result2 = aThenEnd(ParserState("ab"))
 * // Failure(SimpleError("Could not match end")), state.index == 1
 * ```
 */
val end: Parsikle<Unit> = { state ->
    if (state.isEof()) {
        Success(Unit, state)
    } else {
        Failure(SimpleError("Could not match end"), state)
    }
}
