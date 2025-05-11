package io.github.gmulders.parsikle.core

/**
 * Runs this parser, then the given [parser] on the remaining input, and
 * returns a pair of their results.
 *
 * On failure of the second parser, rolls back the input position to where it
 * was after this parser succeeded but preserves any context frames pushed by
 * the second parser.
 *
 * Example:
 * ```kotlin
 * val a: Parsikle<Char> = parse('a')
 * val b: Parsikle<Char> = parse('b').withContext("b-char")
 * val paired: Parsikle<Pair<Char,Char>> = a then b
 * ```
 *
 * @receiver      the first parser
 * @param parser  the second parser to run on the remainder
 * @return        parser that produces a [Pair] of the two results
 */
infix fun <T, V> Parsikle<T>.then(parser: Parsikle<V>): Parsikle<Pair<T, V>> = { state ->
    this(state).then { first, remaining ->
        parser(remaining) /* thenError { error, errorState ->
            Failure(error, errorState.copy(index = remaining.index))
        } */ .map { second ->
            Pair(first, second)
        }
    }
}

/**
 * Runs this parser, then the given [parser], but returns only the result of
 * the second parser.
 *
 * Handy for skipping a prefix or delimiter.
 *
 * Example:
 * ```kotlin
 * val open: Parsikle<Char> = parse('(')
 * val digit: Parsikle<Char> = parse { it.isDigit() }
 * val parser: Parsikle<Char> = open ignoreThen digit
 * // parses "(5" → '5'
 * ```
 *
 * @receiver      the parser whose result is discarded
 * @param parser  the parser whose result is returned
 * @return        parser that yields only the second parser’s result
 */
infix fun <T, V> Parsikle<T>.ignoreThen(parser: Parsikle<V>): Parsikle<V> =
    (this then parser)
        .map { (_, v) -> v }

/**
 * Runs this parser, then the given [parser], but returns only the result of
 * the first parser.
 *
 * Handy for skipping a trailing delimiter or suffix.
 *
 * Example:
 * ```kotlin
 * val digit: Parsikle<Char> = parse { it.isDigit() }
 * val close: Parsikle<Char> = parse(')')
 * val parser: Parsikle<Char> = digit thenIgnore close
 * // parses "5)" → '5'
 * ```
 *
 * @receiver      the parser whose result is returned
 * @param parser  the parser whose result is discarded
 * @return        parser that yields only the first parser’s result
 */
infix fun <T, V> Parsikle<T>.thenIgnore(parser: Parsikle<V>): Parsikle<T> =
    (this then parser)
        .map { (t, _) -> t }

/**
 * Parses `open`, then `p`, then `close`, returning only the result of `p`.
 * Useful for surrounding constructs like parentheses, brackets, quotes, etc.
 *
 * Example:
 * ```kotlin
 * // Parser for a number surrounded by parentheses: "(123)"
 * val parenNumber: Parsikle<Int> =
 *   between(parse('('), number, parse(')'))
 *
 * parenNumber(ParserState("(42)"))  // Success(42)
 * ```
 *
 * @param open   parser for the opening delimiter (e.g. '(')
 * @param p      parser for the inner content
 * @param close  parser for the closing delimiter (e.g. ')')
 * @return       a parser yielding only the result of `p`
 */
fun <A, B, C> between(open: Parsikle<A>, p: Parsikle<B>, close: Parsikle<C>): Parsikle<B> =
    open ignoreThen p thenIgnore close

/**
 * Represents a combined parse error when two alternative parsers both fail.
 *
 * Wraps the error from the left-hand parser and the right-hand parser,
 * presenting a unified message indicating neither branch matched.
 *
 * @property left   the error produced by the first parser
 * @property right  the error produced by the fallback parser
 */
data class OrError(val left: Error, val right: Error) : Error {
    override val message: String
        get() = "Could not match either side of or: $left, $right"
}

/**
 * Attempts this parser, backtracking on failure, and tries the given [parser].
 *
 * If this parser succeeds, its result and consumed state are returned.
 * Otherwise, the input position is reset to the original state, and [parser]
 * is applied. If [parser] also fails, its error is wrapped into an [OrError]
 * combining both error messages.
 *
 * Example:
 * ```kotlin
 * val a: Parsikle<Char> = parse('a')
 * val b: Parsikle<Char> = parse('b')
 * val eitherAB: Parsikle<Char> = a or b
 *
 * eitherAB(ParserState("b")) // Success('b')
 * eitherAB(ParserState("c")) // Failure(OrError(SimpleError("Expected 'a'"), SimpleError("Expected 'b'")))
 * ```
 *
 * @receiver      the first parser to attempt
 * @param parser  the fallback parser if the first fails
 * @return        a parser that yields either the first parser's result or the
 *                fallback's result, or fails with combined error messages
 *                wrapped in [OrError]
 */
infix fun <A> Parsikle<A>.or(parser: Parsikle<A>): Parsikle<A> = { state ->
    this(state).thenError { first, _ ->
        parser(state).mapError { second -> OrError(first, second) }
    }
}

/**
 * Tries a primary parser `p` followed by any number of alternative parsers,
 * returning the result of the first one that succeeds. If all fail, the final
 * error is an `OrError` chaining each failure.
 *
 * Example:
 * ```kotlin
 * val digit = parse("Expected digit") { it.isDigit() }
 * val letter = parse("Expected letter") { it.isLetter() }
 * val parser = oneOf(digit, letter)
 *
 * parser(ParserState("5"))  // Success('5')
 * parser(ParserState("x"))  // Success('x')
 * parser(ParserState("#"))  // Failure(OrError(SimpleError("Expected digit"), SimpleError("Expected letter")))
 * ```
 *
 * @param p        the first parser to attempt
 * @param parsers  additional parsers to try in order if the previous ones fail
 * @return         a parser that yields the first successful result or an `OrError`
 */
fun <A> oneOf(p: Parsikle<A>, vararg parsers: Parsikle<A>): Parsikle<A> =
    parsers.fold(p) { a, b -> a or b }

/**
 * Transforms the successful result of this parser using [fn], leaving the
 * parser’s error behavior and state management intact.
 *
 * Example:
 * ```kotlin
 * val digit: Parsikle<Char> = parse("Expected digit") { it.isDigit() }
 * val asInt: Parsikle<Int> = digit.map { it.digitToInt() }
 * ```
 *
 * @receiver  this parser producing a value of type `T`
 * @param fn  function to convert a `T` into a `V`
 * @return    a parser producing a `V` on success
 */
infix fun <T, V> Parsikle<T>.map(fn: (T) -> V): Parsikle<V> = { state -> this(state).map(fn) }

/**
 * Operator alias for [`map`], allowing you to write `parser { fn }` instead
 * of `parser.map { fn }`.
 *
 * Example:
 * ```kotlin
 * val digit: Parsikle<Char> = parse("Expected digit") { it.isDigit() }
 * val asInt: Parsikle<Int> = digit { it.digitToInt() }
 * ```
 *
 * @receiver  this parser producing a value of type `T`
 * @param fn  function to convert a `T` into a `V`
 * @return    a parser producing a `V` on success
 */
infix operator fun <T, V> Parsikle<T>.invoke(fn: (T) -> V): Parsikle<V> = this.map(fn)

/**
 * Transforms the error of this parser using [fn] when it fails, leaving the
 * successful result and state unchanged.
 *
 * Example:
 * ```kotlin
 * val p = parse("x") { it == 'x' }
 * val betterError = p.mapError { e -> SimpleError("Could not match the value 'x'") }
 * ```
 *
 * @receiver  this parser which may fail with an `Error`
 * @param fn  function to convert one `Error` into another
 * @return    a parser that applies `fn` to any failure
 */
infix fun <T> Parsikle<T>.mapError(fn: (Error) -> Error): Parsikle<T> = { state ->
    this(state).mapError(fn)
}

/**
 * Produces a parser that always succeeds with the given value [a], without
 * consuming any input.
 *
 * Example:
 * ```kotlin
 * val p = succeed(37)
 * p(ParserState("anything"))  // Success(37), index = 0
 * ```
 *
 * @param a  the constant value to return on success
 * @return   a parser that yields `a` and leaves the state untouched
 */
fun <A> succeed(a: A): Parsikle<A> = { state -> Success(a, state) }

/**
 * Always fails with the given [error], without consuming any input.
 *
 * Useful for embedding explicit failures in parser definitions,
 * e.g. in conditional or fallback logic.
 *
 * Example:
 * ```kotlin
 * val alwaysFail: Parsikle<Int> = fail(SimpleError("unrecoverable"))
 * ```
 *
 * @param error  the `Error` instance to return on parse invocation
 * @return       a parser that yields `Failure(error, state)` immediately
 */
fun <A> fail(error: Error): Parsikle<A> = { state -> Failure(error, state) }

/**
 * Always fails with a `SimpleError` constructed from the given [error] message,
 * without consuming any input.
 *
 * Convenience overload for quick error creation.
 *
 * Example:
 * ```kotlin
 * val missingDigit: Parsikle<Char> = fail("Expected a digit here")
 * ```
 *
 * @param error  the error message for the `SimpleError`
 * @return       a parser that yields `Failure(SimpleError(error), state)`
 *               immediately
 */
fun <A> fail(error: String): Parsikle<A> = fail(SimpleError(error))

/**
 * Applicative-style combination of two parsers.
 *
 * Runs this parser (which produces a function of type `(A) -> B`), then runs
 * the provided parser `p` (which produces a value of type `A`), and applies
 * the function to the value, yielding a result of type `B`.
 *
 * Common use case: parsing a curried constructor with successive `.combine`
 * calls.
 *
 * Example:
 * ```kotlin
 * data class Point(val x: Int, val y: Int)
 *
 * // 1) Parser that produces a curried Point constructor
 * val pointConstructor: Parsikle<(Int) -> (Int) -> Point> =
 *     succeed { x: Int -> { y: Int -> Point(x, y) } }
 *
 * // 2) Numeric parser for Ints
 * val number: Parsikle<Int> = /* ... */
 *
 * // 3) Combine to parse "x,y" into a Point(x, y)
 * val pointParser: Parsikle<Point> =
 *     pointConstructor
 *       .combine(number)
 *       .ignoreThen(parse(','))
 *       .combine(number)
 * ```
 *
 * @receiver  parser that yields a function from A to B
 * @param p   parser that yields the argument for the function
 * @return    parser that yields the result of applying the function to the parsed argument
 */
infix fun <A, B> Parsikle<(A) -> B>.combine(p: Parsikle<A>): Parsikle<B> = { state ->
    this(state).then { fn, remainder1 ->
        p(remainder1).then { a, remainder2 ->
            Success(fn(a), remainder2)
        }
    }
}

/**
 * Applicative-style combination using a curried transform function.
 *
 * Maps this parser’s result using the provided curried function `fn` to
 * produce a parser of `(B) -> C`, then applies the resulting parser to the
 * result of `p`. This is a convenience overload that combines mapping and the
 * infix `combine` call.
 *
 * Common use case: constructing values with two inputs via a function literal:
 *
 * ```kotlin
 * data class Point(val x: Int, val y: Int)
 *
 * val number: Parsikle<Int> = /* ... */
 *
 * // Using a curried constructor
 * val pointParser: Parsikle<Point> =
 *   number.combine(number) { x -> { y -> Point(x, y) } }
 * ```
 *
 * @receiver  parser that yields the first argument
 * @param p   parser that yields the second argument
 * @param fn  (A) -> (B) -> C — a curried function combining `A` and `B` into `C`
 * @return    parser that yields the result of `fn(first, second)`
 */
fun <A, B, C> Parsikle<A>.combine(p: Parsikle<B>, fn: (A) -> (B) -> C): Parsikle<C> =
    (this map fn) combine p

/**
 * Defers the creation of a parser until invocation time.
 *
 * This is essential for defining recursive parsers without causing infinite
 * recursion at initialization. The supplied factory function `fn` is called
 * each time the parser is run.
 *
 * Example – parse zero-or-more 'a' characters into a list:
 * ```kotlin
 * val a: Parsikle<Char> = parse('a')
 * val manyA: Parsikle<List<Char>> = lazy {
 *   // recursive definition:
 *   (a then manyA).map { (c, rest) -> listOf(c) + rest }
 *   or succeed(emptyList())
 * }
 *
 * val result = manyA(ParserState("aaa"))
 * // Success(listOf('a','a','a'))
 * ```
 *
 * @param fn  factory producing the actual parser when run
 * @return    a parser that delegates to the parser returned by `fn`
 */
fun <A> lazy(fn: () -> Parsikle<A>): Parsikle<A> = { state ->
    fn()(state)
}

/**
 * Repeats this parser zero or more times, collecting results into a List.
 *
 * Keeps applying this parser until it fails, then returns all successfully
 * parsed values. Always succeeds (even if no matches).
 *
 * Example:
 * ```kotlin
 * val digits: Parsikle<List<Char>> = digit.many()
 * val result = digits(ParserState("123x"))
 * // Success(value=[‘1’,’2’,’3’], state.index=3)
 * ```
 *
 * @receiver  the parser to repeat
 * @return    parser for a list of zero-or-more results
 */
fun <A> Parsikle<A>.many(): Parsikle<List<A>> =
    this then lazy { this.many() } map { (a, b) -> listOf(a) + b } or succeed(emptyList())

/**
 * Repeats this parser until [terminator] succeeds. Consumes the terminator.
 *
 * Applies this parser repeatedly until [terminator] matches; then returns the
 * list of parsed values and the state after the terminator. If [terminator]
 * matches immediately, returns an empty list.
 *
 * Example:
 * ```kotlin
 * val char: Parsikle<Char> = parse("Expected letter") { it.isLetter() }
 * val comma: Parsikle<Char> = parse("Expected comma") { it == ',' }
 * val beforeComma: Parsikle<List<Char>> = char.manyUntil(comma)
 * val result = beforeComma(ParserState("abc,def"))
 * // Success(value=[‘a’,’b’,’c’], state.index=4)
 * ```
 *
 * @receiver          the parser to repeat
 * @param terminator  parser that stops repetition and is consumed
 * @return            parser for values before the terminator
 */
infix fun <A, B> Parsikle<A>.manyUntil(terminator: Parsikle<B>): Parsikle<List<A>> =
    this.then(lazy { this manyUntil terminator })
        .map { (a, listA) -> listOf(a) + listA }
        .or(terminator map { emptyList() })

/**
 * Repeats this parser one or more times, collecting results into a List.
 *
 * Fails if the parser does not match at least once.
 *
 * Example:
 * ```kotlin
 * val digits: Parsikle<List<Char>> = digit.many1()
 * digits(ParserState("5x")) // Success(value=[‘5’], state.index=1)
 * digits(ParserState("x"))  // Failure("Expected digit")
 * ```
 *
 * @receiver  the parser to repeat at least once
 * @return    a parser for a non-empty list of results
 */
fun <T> Parsikle<T>.many1(): Parsikle<List<T>> =
    (this then this.many()) { (a, b)  -> listOf(a) + b }

/**
 * Peeks at the upcoming input without consuming it.
 *
 * Runs this parser against the current `ParserState`:
 * - On success, returns the parsed value but restores the original state, so
 *   no input is consumed.
 * - On failure, propagates the failure without consuming input.
 *
 * Example:
 * ```kotlin
 * val peekDigit: Parsikle<Char> = digit.lookAhead()
 *
 * // Peek does not consume:
 * val result1 = peekDigit(ParserState("5x"))
 * // Success('5'), state.index == 0
 *
 * // We can then consume the digit:
 * val result2 = digit(result1.state)
 * // Success('5'), state.index == 1
 * ```
 *
 * @receiver  the parser to peek at
 * @return    a parser that returns the same value on success but does not
 *            advance the state
 */
fun <A> Parsikle<A>.lookAhead(): Parsikle<A> = { state ->
    this(state).then { a, _ -> Success(a, state) }
}

/**
 * Applies an additional predicate check to the result of this parser.
 *
 * If this parser succeeds with a value `v`, then:
 * - If `predicate(v)` is true, returns `Success(v, remainderState)`.
 * - Otherwise, returns `Failure(SimpleError(message), originalState)`, where
 *   the index is reset to where it was before this parser ran.
 *
 * This is useful for enforcing extra constraints (e.g. range checks).
 *
 * Example:
 * ```kotlin
 * // Reject '0'
 * val nonZero: Parsikle<Char> = digit.filter("Digit must not be zero") { it != '0' }
 *
 * nonZero(ParserState("5"))  // Success('5'), index = 1
 * nonZero(ParserState("0"))  // Failure("Digit must not be zero"), index = 0
 * ```
 *
 * @receiver         the base parser whose output to test
 * @param message    error message if `predicate` returns false
 * @param predicate  function to validate the parsed value
 * @return           a parser that applies the predicate or fails with the
 *                   given message
 */
fun <A> Parsikle<A>.filter(message: String, predicate: (A) -> Boolean): Parsikle<A> = { state ->
    this(state).then { value, remainder ->
        if (predicate(value)) {
            Success(value, remainder)
        } else {
            Failure(SimpleError(message), remainder.copy(index = state.index))
        }
    }
}

/**
 * Parses a list of values separated by a delimiter, returning all parsed values.
 *
 * Tries three alternatives in order:
 * 1. `(this thenIgnore separator).many() then this` – Parses one-or-more
 *    values with delimiters in between.
 * 2. `this { listOf(it) }` – parses a single value.
 * 3. `succeed(emptyList())` – always succeeds with an empty list.
 *
 * Example:
 * ```kotlin
 * val comma: Parsikle<Char> = parse("Expected comma") { it == ',' }
 * val listParser: Parsikle<List<Char>> = digit separatedBy comma
 *
 * listParser(ParserState("1,2,3"))  // Success(listOf('1','2','3'))
 * listParser(ParserState("5"))      // Success(listOf('5'))
 * listParser(ParserState(""))       // Success(emptyList())
 * ```
 *
 * @receiver         parser for individual values
 * @param separator  the parser for the delimiter to skip
 * @return           a parser that returns the list of parsed values
 */
fun <S, T> Parsikle<T>.separatedBy(separator: Parsikle<S>): Parsikle<List<T>> =
        oneOf(
            ((this thenIgnore separator).many() then this) { (list, t) -> list + t },
            this { listOf(it) },
            succeed(emptyList()),
        )

/**
 * Parses one occurrence of `p`, then zero-or-more occurrences of `separator`
 * and `p`, returning a pair of the list of parsed values and the list of
 * parsed separators.
 *
 * Example:
 * ```kotlin
 * val comma: Parsikle<Char> = parse("Expected comma") { it == ',' }
 * val sepParser: Parsikle<Pair<List<Char>, List<Char>>> = separated(digit, comma)
 *
 * sepParser(ParserState("1,2,3"))
 * // Success(Pair(listOf('1','2','3'), listOf(',',',')))
 * ```
 *
 * @param p          parser for the values of type `T`
 * @param separator  parser for the separators of type `S`
 * @return           a parser producing values and separators
 */
fun <T, S> separated(p: Parsikle<T>, separator: Parsikle<S>) : Parsikle<Pair<List<T>, List<S>>> =
    (p then (separator then p).many())
        .map { (t, pairs) ->
            val (ss, ts) = pairs.unzip()
            Pair(listOf(t) + ts, ss)
        }

/**
 * Left‐associative folding of parsed terms separated by a delimiter.
 *
 * Parses a sequence of values using parser `p` separated by `separator`, then
 * folds from left to right by applying
 * `transform(accumulated, separatorToken, nextTerm)`.
 *
 * For input `t1 sep t2 sep t3`, the result is:
 * ```
 * transform(transform(t1, sep, t2), sep, t3)
 * ```
 *
 * Example:
 * ```kotlin
 * // Parser for one‐or‐more digits → Int
 * val number: Parsikle<Int> = /* ... */
 * // Parser for '-' separators
 * val dash: Parsikle<Char> = parse('-')
 *
 * // (10 - 3) - 2 = 5
 * val leftSub: Parsikle<Int> =
 *     leftAssociate(number, dash) { acc, _, term -> acc - term }
 *
 * val result = leftSub(ParserState("10-3-2"))
 * // result is Success(5)
 * ```
 *
 * @param p          parser for the terms of type `T`
 * @param separator  parser for the separator tokens of type `S`
 * @param transform  function combining an accumulated `T`, an `S`, and a `T`
 *                   into a new `T`
 * @return           a parser that yields a left‐associated `T` result
 */
fun <T, S> leftAssociate(p: Parsikle<T>, separator: Parsikle<S>, transform: (T, S, T) -> T) : Parsikle<T> =
    separated(p, separator)
        .map { (terms, separators) ->
            terms.reduceIndexed { index, acc, t ->
                transform(acc, separators[index - 1], t)
            }
        }

/**
 * Right‐associative folding of parsed terms separated by a delimiter.
 *
 * Parses a sequence of values using parser `p` separated by `separator`, then
 * folds from right to left by applying
 * `transform(nextTerm, separatorToken, accumulated)`.
 *
 * For input `t1 sep t2 sep t3`, the result is:
 * ```
 * transform(t1, sep, transform(t2, sep, t3))
 * ```
 *
 * Example:
 * ```kotlin
 * // Parser for one‐or‐more digits → Int
 * val number: Parsikle<Int> = /* ... */
 * // Parser for '-' separators
 * val dash: Parsikle<Char> = parse('-')
 *
 * // 10 - (3 - 2) = 9
 * val rightSub: Parsikle<Int> =
 *     rightAssociate(number, dash) { term, _, acc -> term - acc }
 *
 * val result = rightSub(ParserState("10-3-2"))
 * // result is Success(9)
 * ```
 *
 * @param p          parser for the terms of type `T`
 * @param separator  parser for the separator tokens of type `S`
 * @param transform  function combining a `T`, an `S`, and an accumulated `T`
 *                   into a new `T`
 * @return           a parser that yields a right‐associated `T` result
 */
fun <T, S> rightAssociate(p: Parsikle<T>, separator: Parsikle<S>, transform: (T, S, T) -> T) : Parsikle<T> =
    separated(p, separator)
        .map { (terms, separators) ->
            terms.reduceRightIndexed { index, acc, t ->
                transform(acc, separators[index], t)
            }
        }

/**
 * Left‐associative chaining of binary operator parsers.
 *
 * Parses a first operand, then zero-or-more repetitions of
 * `(operator → function, nextOperand)`.
 * Folds the sequence from the left so that
 * `t1 ∘ t2 ∘ t3` becomes `op1(op2(t1, t2), t3)`.
 *
 * Example:
 * ```kotlin
 * // Parser for '-' producing subtraction function
 * val subOp: Parsikle<(Int, Int) -> Int> = parse('-').map { { a, b -> a - b } }
 * val num: Parsikle<Int> = digit.many1().map { it.joinToString("").toInt() }
 *
 * // Left-associative subtraction: (10 - 3) - 2 = 5
 * val leftSub = num.chainLeft(subOp)
 * val result = leftSub(ParserState("10-3-2"))
 * // Success(5)
 * ```
 *
 * @receiver  parser for the operands
 * @param op  parser that yields a function combining two `A`s
 * @return    parser performing a left-associative fold of all parsed
 *            operators and operands
 */
fun <A> Parsikle<A>.chainLeft(op: Parsikle<(A, A) -> A>): Parsikle<A> =
    this.then((op then this).many())
        .map { (first, rest) ->
            rest.fold(first) { acc, (fn, next) -> fn(acc, next) }
        }

/**
 * Right-associative chaining of binary operator parsers.
 *
 * Parses a head value, then zero-or-more repetitions of
 * `(operator → function, nextValue)`.
 * Folds the list from the right so that
 * `t1 ∘ t2 ∘ t3` becomes `op1(t1, op2(t2, t3))`.
 *
 * Example:
 * ```kotlin
 * // Suppose '-' is parsed into a function that subtracts
 * val subOp: Parsikle<(Int,Int)->Int> = parse('-').map { { a, b -> a - b } }
 * val num: Parsikle<Int> = digit.many1().map { it.joinToString("").toInt() }
 *
 * val rightSub = num.chainRight(subOp)
 * rightSub(ParserState("10-3-2"))
 * // computes 10 - (3 - 2) = 9
 * ```
 *
 * @receiver  parser for the operands
 * @param op  parser that yields a function combining two `A`s
 * @return    parser performing a right-associative fold of all parsed
 *            operators and operands
 */
fun <A> Parsikle<A>.chainRight(op: Parsikle<(A, A) -> A>): Parsikle<A> =
    this.then((op then this).many())
        .map { (first, rest) ->
            if (rest.isEmpty()) first
            else {
                val terms = listOf(first) + rest.map { it.second }
                val ops   = rest.map { it.first }
                terms.reduceRightIndexed { index, term, acc ->
                    if (index == terms.lastIndex) term
                    else ops[index](term, acc)
                }
            }
        }

/**
 * Sequences three parsers in order and collects their results into a `Triple`.
 *
 * Equivalent to `(p1 then p2 then p3)` with a mapping that unpacks the nested
 * pairs into a `Triple<A, B, C>`.
 *
 * @param p1  first parser producing `A`
 * @param p2  second parser producing `B`
 * @param p3  third parser producing `C`
 * @return    a parser that yields `Triple&lt;A, B, C&gt;` on success
 */
fun <A, B, C> consecutive(p1: Parsikle<A>, p2: Parsikle<B>, p3: Parsikle<C>): Parsikle<Triple<A, B, C>> =
    (p1 then p2 then p3) { (ab, c) ->
        val (a, b) = ab
        Triple(a, b, c)
    }

data class Tuple4<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
) {
    override fun toString(): String =
        "($first, $second, $third, $fourth)"
}

/**
 * Sequences four parsers and collects their results into a `Tuple4`.
 *
 * Delegates to the three-argument `consecutive` and then combines with the
 * fourth parser.
 *
 * @see consecutive(p1, p2, p3)
 */
fun <A, B, C, D> consecutive(
    p1: Parsikle<A>,
    p2: Parsikle<B>,
    p3: Parsikle<C>,
    p4: Parsikle<D>,
): Parsikle<Tuple4<A, B, C, D>> =
    (consecutive(p1, p2, p3) then p4) { (abc, d) ->
        val (a, b, c) = abc
        Tuple4(a, b, c, d)
    }

data class Tuple5<out A, out B, out C, out D, out E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
) {
    override fun toString(): String =
        "($first, $second, $third, $fourth, $fifth)"
}

/**
 * Sequences five parsers into a `Tuple5`.
 *
 * @see consecutive(p1, p2, p3)
 */
fun <A, B, C, D, E> consecutive(
    p1: Parsikle<A>,
    p2: Parsikle<B>,
    p3: Parsikle<C>,
    p4: Parsikle<D>,
    p5: Parsikle<E>,
): Parsikle<Tuple5<A, B, C, D, E>> =
    (consecutive(p1, p2, p3, p4) then p5) { (abcd, e) ->
        val (a, b, c, d) = abcd
        Tuple5(a, b, c, d, e)
    }

data class Tuple6<out A, out B, out C, out D, out E, out F>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
    val sixth: F,
) {
    override fun toString(): String =
        "($first, $second, $third, $fourth, $fifth, $sixth)"
}

/**
 * Sequences six parsers into a `Tuple6`.
 *
 * @see consecutive(p1, p2, p3)
 */
fun <A, B, C, D, E, F> consecutive(
    p1: Parsikle<A>,
    p2: Parsikle<B>,
    p3: Parsikle<C>,
    p4: Parsikle<D>,
    p5: Parsikle<E>,
    p6: Parsikle<F>,
): Parsikle<Tuple6<A, B, C, D, E, F>> =
    (consecutive(p1, p2, p3, p4, p5) then p6) { (abcde, f) ->
        val (a, b, c, d, e) = abcde
        Tuple6(a, b, c, d, e, f)
    }

data class Tuple7<out A, out B, out C, out D, out E, out F, out G>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
    val sixth: F,
    val seventh: G,
) {
    override fun toString(): String =
        "($first, $second, $third, $fourth, $fifth, $sixth, $seventh)"
}

/**
 * Sequences seven parsers into a `Tuple7`.
 *
 * @see consecutive(p1, p2, p3)
 */
fun <A, B, C, D, E, F, G> consecutive(
    p1: Parsikle<A>,
    p2: Parsikle<B>,
    p3: Parsikle<C>,
    p4: Parsikle<D>,
    p5: Parsikle<E>,
    p6: Parsikle<F>,
    p7: Parsikle<G>,
): Parsikle<Tuple7<A, B, C, D, E, F, G>> =
    (consecutive(p1, p2, p3, p4, p5, p6) then p7) { (abcdef, g) ->
        val (a, b, c, d, e, f) = abcdef
        Tuple7(a, b, c, d, e, f, g)
    }

data class Tuple8<out A, out B, out C, out D, out E, out F, out G, out H>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
    val sixth: F,
    val seventh: G,
    val eighth: H,
) {
    override fun toString(): String =
        "($first, $second, $third, $fourth, $fifth, $sixth, $seventh, $eighth)"
}

/**
 * Sequences eight parsers into a `Tuple8`.
 *
 * @see consecutive(p1, p2, p3)
 */
fun <A, B, C, D, E, F, G, H> consecutive(
    p1: Parsikle<A>,
    p2: Parsikle<B>,
    p3: Parsikle<C>,
    p4: Parsikle<D>,
    p5: Parsikle<E>,
    p6: Parsikle<F>,
    p7: Parsikle<G>,
    p8: Parsikle<H>,
): Parsikle<Tuple8<A, B, C, D, E, F, G, H>> =
    (consecutive(p1, p2, p3, p4, p5, p6, p7) then p8) { (abcdefg, h) ->
        val (a, b, c, d, e, f, g) = abcdefg
        Tuple8(a, b, c, d, e, f, g, h)
    }

/**
 * Wraps this parser in a named context frame.
 *
 * Pushes `name` onto the `ParserState` context stack before running this
 * parser, then pops the context frame on success (or leaves it on failure) so
 * errors include the provided context name.
 *
 * @receiver    the parser to annotate
 * @param name  the context label to push before parsing
 * @return      a new parser that manages the context frame around this parser
 */
fun <T> Parsikle<T>.withContext(name: String): Parsikle<T> = { state ->
    this(state.pushContext(name)).popContext()
}

/**
 * Skips any leading whitespace (defined by `whiteSpace`) before running
 * [parser].
 *
 * Consumes zero-or-more whitespace characters, then applies [parser],
 * returning only the parser’s result. Useful for ignoring irrelevant spaces
 * between tokens.
 *
 * Example:
 * ```kotlin
 * val digit: Parsikle<Char> = parse("Expected digit") { it.isDigit() }
 * val parser: Parsikle<Char> = ignoreWhitespace(digit)
 *
 * parser(ParserState("   5")) // Success('5'), index=4
 * parser(ParserState("5"))    // Success('5'), index=1
 * parser(ParserState("   x")) // Failure("Expected digit"), index=3
 * ```
 *
 * @param parser  the parser to apply after skipping whitespace
 * @return        a parser that ignores leading whitespace then runs [parser]
 */
fun <T> ignoreWhitespace(parser: Parsikle<T>) : Parsikle<T> = whiteSpace ignoreThen parser

/**
 * Makes this parser optional: if it succeeds, returns the parsed value; if it
 * fails, backtracks and returns `null` without consuming input.
 *
 * Example:
 * ```kotlin
 * // Parser for a single digit, optional
 * val digitOpt: Parsikle<Char?> = digit.optional()
 *
 * digitOpt(ParserState("7a"))  // Success('7')
 * digitOpt(ParserState("x7"))  // Success(null)
 * ```
 *
 * @receiver  the parser to make optional
 * @return    a parser yielding the parsed `T` or `null` on failure
 */
fun <T> Parsikle<T>.optional(): Parsikle<T?> =
    this.map { it as T? } or succeed(null)

/**
 * Captures the exact substring consumed by this parser.
 *
 * Runs this parser, and on success returns the substring from the original
 * input corresponding to everything it consumed. On failure, propagates
 * the original error and state.
 *
 * Example:
 * ```kotlin
 * val ident = number.slice()
 *
 * ident(ParserState("37abc"))
 * // Success("37") // Number as a String
 * ```
 *
 * @receiver  the parser whose consumed text to capture
 * @return    a parser that yields the raw lexeme as String
 */
fun <T> Parsikle<T>.slice(): Parsikle<String> = { state ->
    val start = state.index
    when (val res = this(state)) {
        is Success -> {
            val end = res.state.index
            Success(state.source.substring(start, end), res.state)
        }
        is Failure -> Failure(res.error, res.state)
    }
}

/**
 * Skips any surrounding whitespace before and after this parser.
 *
 * Useful when your grammar allows optional spaces around tokens, but you don’t
 * want to sprinkle `whiteSpace` calls everywhere.
 *
 * Example:
 * ```kotlin
 * // Parses an integer with optional spaces around it, e.g. "  123  "
 * val intTok: Parsikle<Int> = number.lexeme()
 *
 * val result = intTok(ParserState("   42   + next"))
 * // Success(42), state.index points right after the trailing spaces
 * ```
 *
 * @receiver  the parser for the core token
 * @return    a parser that ignores whitespace both before and after
 */
fun <T> Parsikle<T>.lexeme(): Parsikle<T> =
    whiteSpace ignoreThen this thenIgnore whiteSpace
