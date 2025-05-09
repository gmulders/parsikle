package io.github.gmulders.parsikle.core

/**
 * A Parsikle parser is a function that consumes a [ParserState] and returns a
 * [Result] carrying either a successful value of type [R] or an [Error].
 *
 * Every primitive parser and every composed parser built by combinators
 * adheres to this shape. Combinator functions accept and return values
 * of this type to build more complex grammars from simpler ones.
 *
 * @param R The type of the parsed result
 */
typealias Parsikle<R> = (ParserState) -> Result<Error, R>
