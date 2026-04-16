package io.github.gmulders.parsikle.test

import io.github.gmulders.parsikle.core.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

fun <R> assertThat(parser: Parsikle<R>): ParsikleAssert<R> =
    ParsikleAssert(parser)

class ParsikleAssert<R>(val parser: Parsikle<R>) {
    fun whenParses(s: String): ResultAssert<R> {
        val state = ParserState(s)
        val result = parser(state)
        return ResultAssert(parser, result, state)
    }
}

class ResultAssert<R>(val parser: Parsikle<R>, val result: Result<Error, R>, val initialState: ParserState) {
    fun succeeds(): SuccessResultAssert<Error, R> {
        return when(result) {
            is Success -> SuccessResultAssert(result, initialState).withEmptyContext()
            is Failure -> fail("Result should be Success, but parse failed: ${(result as Failure<Error, R>).error.message}")
        }
    }

    inline fun <reified E : Error> fails(): FailedResultAssert<E, R> {
        assertTrue(result is Failure, "Expected result to be a failure")
        assertTrue(result.error is E, "Expected error to be ${E::class}, but was ${result.error::class}")
        return FailedResultAssert(result as Failure<E, R>, initialState)
    }
}

class SuccessResultAssert<E: Error, R>(val result: Success<E, R>, val initialState: ParserState) {
    fun withEmptyContext(): SuccessResultAssert<E, R> {
        assertTrue(result.state.context.isEmpty(), "Expected empty context, but was ${result.state.context}")
        return this
    }

    fun withNoRemainder(): SuccessResultAssert<E, R> =
        withRemainder("")

    fun withRemainder(remainder: String): SuccessResultAssert<E, R> {
        assertEquals(remainder, result.state.remainder, "Expected remainder '$remainder', but was '${result.state.remainder}'")
        return this
    }

    fun withValue(value: R): SuccessResultAssert<E, R> {
        assertEquals(value, result.value, "Expected value '$value', but was '${result.value}'")
        return this
    }

    fun withValue(failure: String, fn: (R) -> Boolean): SuccessResultAssert<E, R> {
        assertTrue(fn(result.value), failure)
        return this
    }

    fun at(line: Int, column: Int): SuccessResultAssert<E, R> {
        assertEquals(line, result.state.line, "Expected line '$line', but was '${result.state.line}'")
        assertEquals(column, result.state.column, "Expected column '$column', but was '${result.state.column}'")
        return this
    }

    fun withFurthestIndex(index: Int): SuccessResultAssert<E, R> {
        assertEquals(index, initialState.tracker.furthestIndex, "Expected furthest index '$index', but was '${initialState.tracker.furthestIndex}'")
        return this
    }

    fun withFurthestError(error: Error): SuccessResultAssert<E, R> {
        assertEquals(error, initialState.tracker.furthestError, "Expected furthest error '$error', but was '${initialState.tracker.furthestError}'")
        return this
    }
}

class FailedResultAssert<E : Error, R>(val result: Failure<E, R>, val initialState: ParserState) {
    fun withError(error: E): FailedResultAssert<E, R> {
        assertEquals(error, result.error)
        return this
    }

    fun at(line: Int, column: Int): FailedResultAssert<E, R> {
        assertEquals(line, result.state.line, "Expected line '$line', but was '${result.state.line}'")
        assertEquals(column, result.state.column, "Expected column '$column', but was '${result.state.column}'")
        return this
    }

    fun withContext(vararg context: Context): FailedResultAssert<E, R> {
        assertEquals(context.toList(), result.state.context, "Expected context '$context', but was '${result.state.context}'")
        return this
    }

    fun withFurthestIndex(index: Int): FailedResultAssert<E, R> {
        assertEquals(index, initialState.tracker.furthestIndex, "Expected furthest index '$index', but was '${initialState.tracker.furthestIndex}'")
        return this
    }

    fun withFurthestError(error: Error): FailedResultAssert<E, R> {
        assertEquals(error, initialState.tracker.furthestError, "Expected furthest error '$error', but was '${initialState.tracker.furthestError}'")
        return this
    }
}
