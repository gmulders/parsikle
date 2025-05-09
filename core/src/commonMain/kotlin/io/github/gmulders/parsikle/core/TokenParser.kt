package io.github.gmulders.parsikle.core

/**
 * Represents a successful match of a regular expression, tagging it with a token type.
 *
 * @param T        The enum or marker type used to categorize this token
 * @property type  The token type associated with this match
 * @property match The [MatchResult] returned by the regex engine
 * @property matchValue  Convenience getter for [match.value], the matched substring
 */
data class TokenMatch<T>(val type: T, val match: MatchResult) {
    val matchValue: String get() = match.value
}

/**
 * Indicates that a regular expression failed to match at the current parse position.
 *
 * @property regex    The [Regex] attempted against the input
 * @property haystack The remaining input as a [CharSequence] at match time
 */
data class RegularExpressionError(val regex: Regex, val haystack: CharSequence) : Error {
    override val message: String
        get() = "Could not match regex '$regex' in '$haystack'"
}

/**
 * Parses the input using the given regular expression [re], returning a [TokenMatch]
 * of type [T] on success or a [RegularExpressionError] on failure.
 *
 * The regex is applied to the remaining input (from the current state index):
 * - On success, consumes the entire match and advances the state.
 * - On failure, returns a `Failure` with the original state unchanged.
 *
 * Example:
 * ```kotlin
 * enum class Token { NUMBER }
 * // Match one or more digits as type NUMBER
 * val numberToken: Parsikle<TokenMatch<Token>> =
 *     tokenParser(Regex("\\d+"), NUMBER)
 *
 * val state1 = ParserState("123abc")
 * val result1 = numberToken(state1)
 * // Success(TokenMatch("NUMBER", match=MatchResult("123")), state.index == 3)
 *
 * val state2 = ParserState("abc123")
 * val result2 = numberToken(state2)
 * // Failure(RegularExpressionError(Regex("\\d+"), "abc123"), state.index == 0)
 * ```
 *
 * @param re   The regular expression to apply at the current position
 * @param type A marker value of type `T` to tag the matched token
 * @return     A parser that yields a `TokenMatch<T>` on match or fails with `RegularExpressionError`
 */
fun <T> tokenParser(re: Regex, type: T): Parsikle<TokenMatch<T>> = { state ->
    val remainder = state.source.subSequence(state.index, state.source.length)
    val match = re.find(remainder)
    if (match == null) {
        Failure(RegularExpressionError(re, remainder), state)
    } else {
        Success(TokenMatch(type, match), state.next(match.range.last + 1))
    }
}
