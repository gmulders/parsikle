package io.github.gmulders.parsikle

fun parse(failure: String, predicate: (Char) -> Boolean): Parsikle<Char> = { state ->
    if (state.isNotEof() && predicate(state.nextChar())) {
        Success(state.nextChar(), state.next())
    } else {
        Failure(SimpleError(failure), state)
    }
}

fun parse(char: Char): Parsikle<Char> = parse("Expected '$char'") { input -> input == char }
