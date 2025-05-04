package io.github.gmulders.parsikle.core

import arrow.core.Either
import arrow.core.left
import arrow.core.right

interface Error {
    val message: String
}

sealed class Result<out X : Error, out V>(open val state: ParserState) {
    infix fun <T> then(transform: (V, ParserState) -> Result<@UnsafeVariance X, T>): Result<X, T> {
        return when (this) {
            is Success -> transform(value, state)
            is Failure -> Failure(error, state)
        }
    }

    infix fun <T : Error> thenError(transform: (X, ParserState) -> Result<T, @UnsafeVariance V>): Result<T, V> {
        return when (this) {
            is Success -> Success(value, state)
            is Failure -> transform(error, state)
        }
    }

    infix fun <T> map(transform: (V) -> T): Result<X, T> {
        return when (this) {
            is Success -> Success(transform(value), state)
            is Failure -> Failure(error, state)
        }
    }

    infix fun <T : Error> mapError(transform: (X) -> T): Result<T, V> {
        return when (this) {
            is Success -> Success(value, state)
            is Failure -> Failure(transform(error), state)
        }
    }

    fun popContext(): Result<X, V> {
        return when (this) {
            is Success -> Success(value, state.popContext())
            is Failure -> Failure(error, state)
        }
    }

    fun toEither(): Either<X, V> {
        return when (this) {
            is Success -> value.right()
            is Failure -> error.left()
        }
    }
}

data class Failure<X : Error, T>(val error: X, override val state: ParserState) : Result<X, T>(state)
data class Success<X : Error, T>(val value: T, override val state: ParserState) : Result<X, T>(state)
