package io.github.gmulders.parsikle.test

import io.github.gmulders.parsikle.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

fun <R> assertThat(parser: Parsikle<R>): ParsikleAssert<R> =
    ParsikleAssert(parser)

class ParsikleAssert<R>(val parser: Parsikle<R>) {
    fun whenParses(s: String): ResultAssert<R> {
        val result = parser(ParserState(s))
        return ResultAssert(parser, result)
    }
}

class ResultAssert<R>(val parser: Parsikle<R>, val result: Result<Error, R>) {
    fun succeeds(): SuccessResultAssert<Error, R> {
        return when(result) {
            is Success -> SuccessResultAssert(result)
            is Failure -> fail("Result should be Success, but parse failed: ${(result as Failure<Error, R>).error.message}")
        }
    }

    inline fun <reified E : Error> fails(): FailedResultAssert<E, R> {
        assertTrue(result is Failure, "Expected result to be a failure")
        assertTrue(result.error is E, "Expected error to be ${E::class}, but was ${result.error::class}")
        return FailedResultAssert(result as Failure<E, R>)
    }
}

class SuccessResultAssert<E: Error, R>(val result: Success<E, R>) {
    fun withNoRemainder(): SuccessResultAssert<E, R> =
        withRemainder("")

    fun withRemainder(remainder: String): SuccessResultAssert<E, R> {
        assertEquals(result.state.remainder, remainder, "Expected remainder '$remainder', but was '${result.state.remainder}'")
        return this
    }

    fun withValue(value: R): SuccessResultAssert<E, R> {
        assertEquals(result.value, value, "Expected value '$value', but was '${result.value}'")
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
}

class FailedResultAssert<E : Error, R>(val result: Failure<E, R>) {
    fun withError(error: E): FailedResultAssert<E, R> {
        assertEquals(result.error, error)
        return this
    }

    fun at(line: Int, column: Int): FailedResultAssert<E, R> {
        assertEquals(line, result.state.line, "Expected line '$line', but was '${result.state.line}'")
        assertEquals(column, result.state.column, "Expected column '$column', but was '${result.state.column}'")
        return this
    }
}
