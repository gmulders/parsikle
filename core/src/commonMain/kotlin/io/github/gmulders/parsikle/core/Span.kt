package io.github.gmulders.parsikle.core

import kotlin.lazy

/**
 * A region in the source text, identified by a [start] and [end] index.
 *
 * Provides lazy [line] and [column] properties for human-readable error
 * reporting.
 *
 * @property source The full input text
 * @property start  The start index (inclusive) of this span
 * @property end    The end index (exclusive) of this span
 */
data class Span(val source: String, val start: Int, val end: Int) {

    /**
     * The line number (1-based) at [start], counting '\n' boundaries.
     */
    val line: Int by lazy { source.subSequence(0, start).lines().count() }

    /**
     * The column number (1-based) within the line at [start].
     */
    val column: Int by lazy { source.subSequence(0, start).lines().last().length + 1 }

    /**
     * The substring of [source] covered by this span.
     */
    val text: String by lazy { source.substring(start, end) }
}

/**
 * A value of type [T] annotated with the [Span] of source text it was parsed
 * from.
 *
 * Use the [spanned] combinator to wrap any parser's result in a [Spanned].
 *
 * @property value The parsed value
 * @property span  The source region that produced [value]
 */
data class Spanned<T>(val value: T, val span: Span)
