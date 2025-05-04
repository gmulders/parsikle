package io.github.gmulders.parsikle.core

data class TokenMatch<T>(val type: T, val match: MatchResult) {
    val matchValue: String get() = match.value
}

data class RegularExpressionError(val regex: Regex, val haystack: CharSequence) : Error {
    override val message: String
        get() = "Could not match regex '$regex' in '$haystack'"
}

fun <T> tokenParser(re: Regex, type: T): Parsikle<TokenMatch<T>> = { state ->
    val remainder = state.source.subSequence(state.index, state.source.length)
    val match = re.find(remainder)
    if (match == null) {
        Failure(RegularExpressionError(re, remainder), state)
    } else {
        Success(TokenMatch(type, match), state.next(match.range.last + 1))
    }
}
