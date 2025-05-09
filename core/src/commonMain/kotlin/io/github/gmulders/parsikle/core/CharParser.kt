package io.github.gmulders.parsikle.core

/**
 * Creates a parser that consumes a single character if it satisfies the given
 * [predicate], or fails with a [[SimpleError]] carrying the provided [failure]
 * message.
 *
 * On success, returns `Success(matchedChar, newState)` where `newState` is
 * advanced by one.
 * On failure (EOF or predicate false), returns
 * `Failure(SimpleError(failure), state)` without consuming input.
 *
 * Example:
 * ```kotlin
 * // Parser for any uppercase letter
 * val upper: Parsikle<Char> = parse("Expected uppercase") { it.isUpperCase() }
 *
 * val result1 = upper(ParserState("Ahello"))
 * // Success('A'), state.index == 1
 *
 * val result2 = upper(ParserState("aXYZ"))
 * // Failure(SimpleError("Expected uppercase")), state.index == 0
 * ```
 *
 * @param failure    error message when the next character does not satisfy the predicate
 * @param predicate  function to test the next input character
 * @return           a parser that matches one character or fails
 */
fun parse(failure: String, predicate: (Char) -> Boolean): Parsikle<Char> = { state ->
    if (state.isNotEof() && predicate(state.nextChar())) {
        Success(state.nextChar(), state.next())
    } else {
        Failure(SimpleError(failure), state)
    }
}

/**
 * Creates a parser that matches exactly the specified [char], or fails with a
 * default message.
 *
 * Delegates to the `parse(failure, predicate)` overload using message
 * `"Expected '$char'"`.
 *
 * Example:
 * ```kotlin
 * val openParen: Parsikle<Char> = parse('(')
 *
 * openParen(ParserState("(x"))  // Success('('), index == 1
 * openParen(ParserState("x("))  // Failure(SimpleError("Expected '('")), index == 0
 * ```
 *
 * @param char  the exact character to match
 * @return      a parser that consumes [char] or fails
 */
fun parse(char: Char): Parsikle<Char> = parse("Expected '$char'") { input -> input == char }

/**
 * Parses zero-or-more characters that satisfy [predicate], returning them as
 * a String.
 *
 * Repeatedly applies `parse(name, predicate)`:
 * - On each success, captures the Char and recurses.
 * - Stops when the predicate fails and returns the concatenated String.
 * Always succeeds (never returns Failure).
 *
 * Example:
 * ```kotlin
 * val letters: Parsikle<String> = parseWhile("letter", { it.isLetter() })
 *
 * letters(ParserState("abc123"))  // Success("abc"), index=3
 * letters(ParserState("123"))     // Success(""), index=0
 * ```
 *
 * @param name       error message to use if the first character does not match
 *                   (though eventual failure is recovered)
 * @param predicate  function to test each input Char
 * @return           a parser that returns the longest prefix of matching chars
 */
fun parseWhile(name: String, predicate: (Char) -> Boolean): Parsikle<String> =
    parse(name, predicate) then lazy { parseWhile(name, predicate) } map { (a, b) -> a + b } or succeed("")
