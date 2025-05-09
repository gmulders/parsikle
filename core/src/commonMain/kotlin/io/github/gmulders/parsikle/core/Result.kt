package io.github.gmulders.parsikle.core

/**
 * Represents a parse failure in Parsikle.
 *
 * Parsers return a [Result] that may contain an [Error], providing a
 * human-readable [message] describing what went wrong.
 */
interface Error {
    val message: String
}

/**
 * A basic implementation of [Error] that carries only a human-readable
 * message.
 *
 * Use this for simple failure cases where no additional metadata is required.
 *
 * @property message A description of the parse failure.
 */
data class SimpleError(override val message: String) : Error

/**
 * Represents the outcome of running a parser: either a [Success] with a value
 * of type [V], or a [Failure] with an error of type [X]. Always carries the
 * [state] at the point of success or failure.
 *
 * @param X The type of parse error, must implement [Error]
 * @param V The type of the successful parse result
 * @property state The [ParserState] after parsing (or at failure)
 */
sealed class Result<out X : Error, out V>(open val state: ParserState) {

    /**
     * Sequence this result with another parser transform.
     *
     * If this is a [Success], applies [transform] to the parsed value and current state.
     * If this is a [Failure], propagates the same error and state without running [transform].
     *
     * @param transform Function that takes the successful value and state, returning a new [Result]
     * @return A [Result] of type [T], preserving failures or chaining successes
     */
    fun <T> then(transform: (V, ParserState) -> Result<@UnsafeVariance X, T>): Result<X, T> =
        when (this) {
            is Success -> transform(value, state)
            is Failure -> this as Failure<X, T>
        }

    /**
     * Recover or transform errors before continuing.
     *
     * If this is a [Failure], applies [transform] to the error and state to produce a new [Result].
     * If this is a [Success], preserves the value and state without invoking [transform].
     *
     * @param transform Function that takes the error and state, returning a new [Result]
     * @return A [Result] with the same success or a transformed failure
     */
    fun <T : Error> thenError(transform: (X, ParserState) -> Result<T, @UnsafeVariance V>): Result<T, V> =
        when (this) {
            is Success -> this as Success<T, V>
            is Failure -> transform(error, state)
        }

    /**
     * Transform the successful value without affecting errors.
     *
     * If this is a [Success], applies [transform] to the value and returns a new [Success].
     * If this is a [Failure], propagates the same error and state.
     *
     * @param transform Function to convert a value of type [V] to type [T]
     * @return A [Result] with the transformed success value or the original failure
     */
    fun <T> map(transform: (V) -> T): Result<X, T> =
        when (this) {
            is Success -> Success(transform(value), state)
            is Failure -> this as Failure<X, T>
        }

    /**
     * Transform the error without affecting successful values.
     *
     * If this is a [Failure], applies [transform] to the error and returns a new [Failure].
     * If this is a [Success], preserves the same value and state.
     *
     * @param transform Function to convert an error of type [X] to type [T]
     * @return A [Result] with the transformed error or the original success
     */
    fun <T : Error> mapError(transform: (X) -> T): Result<T, V> =
        when (this) {
            is Success -> this as Success<T, V>
            is Failure -> Failure(transform(error), state)
        }

    /**
     * Pop the most recent parse context frame on success.
     *
     * If this is a [Success], calls `popContext()` on the stored [state].
     * If this is a [Failure], leaves the state and error unchanged.
     *
     * @return A [Result] with its context frame popped on success
     */
    fun popContext(): Result<X, V> =
        when (this) {
            is Success -> Success(value, state.popContext())
            is Failure -> this as Failure<X, V>
        }
}

/**
 * Indicates a parser failure with a specific [error] and the [state] at failure.
 *
 * @param X The error type
 * @param T Unused type parameter for the would-be successful value
 * @property error The parse error
 * @property state The [ParserState] when the failure occurred
 */
data class Failure<X : Error, T>(val error: X, override val state: ParserState) : Result<X, T>(state)

/**
 * Indicates a parser success with a specific [value] and the [state] after consumption.
 *
 * @param X The error type (unused in success)
 * @param T The type of the parsed value
 * @property value The successfully parsed result
 * @property state The [ParserState] after parsing
 */
data class Success<X : Error, T>(val value: T, override val state: ParserState) : Result<X, T>(state)
