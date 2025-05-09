package io.github.gmulders.parsikle.core

/**
 * Represents the current state of the parser: the input string, the current
 * index into that string, and a stack of named contexts for error reporting.
 *
 * @property source   The full input being parsed
 * @property index    The current position (0-based) in [source]
 * @property context  A list of [Context] frames that have been pushed but not
 *                    yet popped, used to annotate parse errors
 *
 * @constructor    Creates a new state at [index] 0 with an empty context.
 * @param source   The input text
 * @param index    The current cursor position
 * @param context  The parse context stack
 */
data class ParserState(
    val source: String,
    val index: Int = 0,
    val context: List<Context> = emptyList(),
) {
    /**
     * The remainder of the input from [index] to end
     */
    val remainder by kotlin.lazy { source.drop(index) }

    /**
     * The line number (1-based) at [index], counting '\n' boundaries
     */
    val line: Int by kotlin.lazy { source.subSequence(0, index).lines().count() }

    /**
     * The column number (1-based) within the current line
     */
    val column: Int by kotlin.lazy { source.subSequence(0, index).lines().last().length + 1 }

    /** @return true if [index] is at the end of [source] */
    fun isEof(): Boolean = source.length == index

    /** @return true if there is more input to consume */
    fun isNotEof(): Boolean = !isEof()

    /** @return the character at the current [index] */
    fun nextChar(): Char = source[index]

    /**
     * Advance the cursor by [add] characters
     *
     * @param add Number of characters to skip
     * @return A new [ParserState] with [index] increased by [add]
     */
    fun next(add: Int): ParserState = copy(index = index + add)

    /**
     * Advance the cursor by one character.
     *
     * @return A new [ParserState] with [index] increased by 1
     */
    fun next(): ParserState = next(1)

    /**
     * Push a new context frame with the given [name] at the current [index].
     *
     * This is used to annotate nested parsing scopes for improved error messages.
     *
     * @param name A label for the context being entered
     * @return A new [ParserState] with the context frame appended
     */
    fun pushContext(name: String): ParserState = copy(context = context + Context(index, name))

    /**
     * Pop the most recent context frame.
     *
     * @return A new [ParserState] with the last context removed
     */
    fun popContext(): ParserState = copy(context = context.dropLast(1))
}

/**
 * A single named context frame in the parser state.
 *
 * @property pos   The input index where this context was pushed
 * @property name  A descriptive label for the parsing scope
 */
data class Context(val pos: Int, val name: String)
