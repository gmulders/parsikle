package io.github.gmulders.parsikle.core

import arrow.core.Tuple4
import arrow.core.Tuple5
import arrow.core.Tuple6
import arrow.core.Tuple7
import arrow.core.Tuple8

infix fun <T, V> Parsikle<T>.then(parser: Parsikle<V>): Parsikle<Pair<T, V>> = { state ->
    this(state) then { first, remaining ->
        parser(remaining) thenError { error, _ ->
            Failure(error, remaining)
        } map { second ->
            Pair(first, second)
        }
    }
}

infix fun <T, V> Parsikle<T>.ignoreThen(parser: Parsikle<V>): Parsikle<V> =
    (this then parser)
        .map { (_, v) -> v }

infix fun <T, V> Parsikle<T>.thenIgnore(parser: Parsikle<V>): Parsikle<T> =
    (this then parser)
        .map { (t, _) -> t }

infix fun <A> Parsikle<A>.or(parser: Parsikle<A>): Parsikle<A> = { state ->
    this(state) thenError { first, _ ->
        parser(state) mapError { second ->
            SimpleError("${first.message} OR ${second.message}")
        }
    }
}

fun <A> oneOf(p: Parsikle<A>, vararg parsers: Parsikle<A>): Parsikle<A> = parsers.fold(p) { a, b -> a or b}

infix fun <T, V> Parsikle<T>.map(fn: (T) -> V): Parsikle<V> = { state ->
    this(state) map fn
}

infix operator fun <T, V> Parsikle<T>.invoke(fn: (T) -> V): Parsikle<V> = this.map(fn)

infix fun <T> Parsikle<T>.mapError(fn: (Error) -> Error): Parsikle<T> = { state ->
    this(state) mapError fn
}

fun <A> succeed(a: A): Parsikle<A> = { state -> Success(a, state) }

infix fun <A, B> Parsikle<(A) -> B>.combine(p: Parsikle<A>): Parsikle<B> = { state ->
    this(state) then { fn, remainder1 ->
        p(remainder1) then { a, remainder2 ->
            Success(fn(a), remainder2)
        }
    }
}

fun <A, B, C> Parsikle<A>.combine(p: Parsikle<B>, fn: (A) -> (B) -> C): Parsikle<C> =
    (this map fn) combine p

fun <A> lazy(fn: () -> Parsikle<A>): Parsikle<A> = { state ->
    fn()(state)
}

fun <A> Parsikle<A>.many(): Parsikle<List<A>> =
    this then lazy { this.many() } map { (a, b) -> listOf(a) + b } or succeed(emptyList())

infix fun <A, B> Parsikle<A>.manyUntil(p: Parsikle<B>): Parsikle<List<A>> =
    this then
            lazy { this manyUntil p } map { (a, listA) -> listOf(a) + listA } or
            (p map { emptyList() })

fun <T> Parsikle<T>.many1(): Parsikle<List<T>> =
    (this then this.many()) { (a, b)  -> listOf(a) + b }

fun <A> Parsikle<A>.lookAhead(): Parsikle<A> = { state ->
    this(state) then { a, _ -> Success(a, state) }
}

val whiteSpace: Parsikle<String> =
    parse("Expected whitespace") { char -> char.isWhitespace() }.many() map { list -> list.joinToString("") { it.toString() } }

val digit: Parsikle<Char> = parse("Expected a digit") { char -> char.isDigit() }

fun <A> Parsikle<A>.filter(message: String, predicate: (A) -> Boolean): Parsikle<A> = { state ->
    this(state) then { value, remainder ->
        if (predicate(value)) {
            Success(value, remainder)
        } else {
            Failure(SimpleError(message), state)
        }
    }
}

val number: Parsikle<Number> = digit.filter("Number must not start with a '0'") {
    it != '0'
} then digit.many() map { (char, list) ->
    listOf(char) + list
} map { list ->
    list.joinToString("") { it.toString() }
} map { s ->
    s.toInt()
}

fun parseWhile(name: String, predicate: (Char) -> Boolean): Parsikle<String> =
    parse(name, predicate) then lazy { parseWhile(name, predicate) } map { (a, b) -> a + b } or succeed("")

fun <S, T> Parsikle<T>.separatedBy(separator: Parsikle<S>): Parsikle<List<T>> =
        ((this thenIgnore separator).many() then this).map { (list, t) -> list + t } or
        this { listOf(it) } or
        succeed(emptyList())

fun <T, S> separated(p: Parsikle<T>, separator: Parsikle<S>) : Parsikle<Pair<List<T>, List<S>>> =
    (p then (separator then p).many())
        .map { (t, pairs) ->
            val (ss, ts) = pairs.unzip()
            Pair(listOf(t) + ts, ss)
        }

fun <T, S> leftAssociate(p: Parsikle<T>, separator: Parsikle<S>, transform: (T, S, T) -> T) : Parsikle<T> =
    separated(p, separator)
        .map { (terms, separators) ->
            terms.reduceIndexed { index, acc, t ->
                transform(acc, separators[index - 1], t)
            }
        }

fun <T, S> rightAssociate(p: Parsikle<T>, separator: Parsikle<S>, transform: (T, S, T) -> T) : Parsikle<T> =
    separated(p, separator)
        .map { (terms, separators) ->
            terms.reduceRightIndexed { index, acc, t ->
                transform(acc, separators[index], t)
            }
        }

fun <T> ignoreWhitespace(parser: Parsikle<T>) : Parsikle<T> = whiteSpace ignoreThen parser

fun <A, B, C> consecutive(p1: Parsikle<A>, p2: Parsikle<B>, p3: Parsikle<C>): Parsikle<Triple<A, B, C>> =
    (p1 then p2 then p3) { (ab, c) ->
        val (a, b) = ab
        Triple(a, b, c)
    }

fun <A, B, C, D> consecutive(p1: Parsikle<A>, p2: Parsikle<B>, p3: Parsikle<C>, p4: Parsikle<D>): Parsikle<Tuple4<A, B, C, D>> =
    (consecutive(p1, p2, p3) then p4) { (abc, d) ->
        val (a, b, c) = abc
        Tuple4(a, b, c, d)
    }

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

fun <T> Parsikle<T>.withContext(name: String): Parsikle<T> = { state ->
    this(state.pushContext(name)).popContext()
}

fun end(): Parsikle<Unit> = { state ->
    if (state.isEof()) {
        Success(Unit, state)
    } else {
        Failure(SimpleError("Could not match end"), state)
    }
}
