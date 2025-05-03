package io.github.gmulders.parsikle

data class TokenMatch<T>(val type: T, val match: MatchResult) {
    val matchValue: String get() = match.value

//    override fun hashCode(): Int {
//        var result = type?.hashCode() ?: 0
//        result = 31 * result + matchValue.hashCode()
//        return result
//    }
//
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (other == null || other !is TokenMatch<T>) return false
//        if (type != other.type) return false
//        if (matchValue != other.matchValue) return false
//        return true
//    }
}

data class RegularExpressionError(val regex: Regex, val haystack: CharSequence) : Error {
    override val message: String
        get() = "Could not match regex '$regex' in '$haystack'"

//    override fun hashCode(): Int {
//        var result = regex.toString().hashCode()
//        result = 31 * result + regex.options.hashCode()
//        result = 31 * result + haystack.hashCode()
//        return result
//    }
//
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (other == null || other !is RegularExpressionError) return false
//        if (regex.toString() != other.regex.toString()) return false
//        if (regex.options != other.regex.options) return false
//        if (haystack != other.haystack) return false
//        return true
//    }
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
