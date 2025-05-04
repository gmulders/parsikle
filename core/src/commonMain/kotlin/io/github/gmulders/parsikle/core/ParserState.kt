package io.github.gmulders.parsikle.core

data class ParserState(
    val source: String,
    val index: Int,
    val context: List<Context>,
) {
    constructor(source: String) : this(source, 0, emptyList())

    val remainder by kotlin.lazy { source.drop(index) }
    val line: Int by kotlin.lazy { source.subSequence(0, index).lines().count() }
    val column: Int by kotlin.lazy { source.subSequence(0, index).lines().last().length + 1 }

    fun isEof(): Boolean = source.length == index
    fun isNotEof(): Boolean = !isEof()
    fun nextChar(): Char = source[index]

    fun next(add: Int): ParserState = copy(index = index + add)
    fun next(): ParserState = next(1)

    fun pushContext(name: String): ParserState = copy(context = context + Context(index, name))
    fun popContext(): ParserState = copy(context = context.dropLast(1))
}

data class Context(val pos: Int, val name: String)
